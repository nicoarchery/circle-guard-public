# Rollback Plan

## 1. Code Rollback

If a bug is discovered after deployment, revert the code and let the pipeline redeploy:

```bash
# 1. Find the problematic commit
git log --oneline -10

# 2. Revert it
git revert <commit-hash> --no-edit

# 3. Push — Jenkins will automatically rebuild and redeploy
git push origin main
```

If multiple commits need to be rolled back:
```bash
git revert <oldest-commit>..<newest-commit> --no-edit
git push origin main
```

## 2. Kubernetes Rollback

### Microservice Rollback

```bash
# List rollout history for a service
kubectl rollout history deployment/circleguard-auth-service -n circleguard-prod

# Rollback to previous revision
kubectl rollout undo deployment/circleguard-auth-service -n circleguard-prod

# Rollback to a specific revision
kubectl rollout undo deployment/circleguard-auth-service -n circleguard-prod --to-revision=3

# Verify rollback status
kubectl rollout status deployment/circleguard-auth-service -n circleguard-prod

# Verify pods are running
kubectl get pods -n circleguard-prod -l app=circleguard-auth-service
```

### Rollback All Services

```bash
for svc in circleguard-auth-service circleguard-identity-service circleguard-gateway-service circleguard-form-service circleguard-promotion-service circleguard-notification-service; do
  kubectl rollout undo deployment/$svc -n circleguard-prod
done
```

### Rollback via Image Tag

If you know the previous stable version tag:

```bash
# Find the previous version
kubectl get deployment circleguard-auth-service -n circleguard-prod -o jsonpath='{.spec.template.spec.containers[0].image}'

# If it shows v1.0.50 and v1.0.51 is broken, reapply manifests with the old tag
find k8s/prod/ -name "*.yaml" -exec sed -i "s|:prod|:v1.0.50|g" {} +
kubectl apply -f k8s/prod/
# Restore :prod tags after rollback
find k8s/prod/ -name "*.yaml" -exec sed -i "s|:v1.0.50|:prod|g" {} +
git checkout -- k8s/prod/
```

## 3. Database Rollback

### Prerequisites
- PostgreSQL has persistent storage via PVC (`postgresql-data`)
- Regular backups should be scheduled (see backup section below)

### Restore from Backup

```bash
# 1. Identify the backup file (e.g., from Azure Disk snapshot or pg_dump)
# 2. Copy backup to the pod
kubectl cp /path/to/backup.sql circleguard-dev/postgresql-service-xxxxx:/tmp/

# 3. Drop and recreate the database
kubectl exec -n circleguard-dev deployment/postgresql-service -- psql -U admin -d postgres -c "
  DROP DATABASE IF EXISTS circleguard;
  CREATE DATABASE circleguard OWNER admin;
"

# 4. Restore from backup
kubectl exec -n circleguard-dev deployment/postgresql-service -- psql -U admin -d circleguard < /tmp/backup.sql

# 5. Restart all microservices so they reconnect with clean state
kubectl rollout restart deployment -n circleguard-dev -l app
```

### Restore via Azure Disk Snapshot

```bash
# 1. Identify the PVC and PV
kubectl get pvc postgresql-data -n circleguard-dev -o jsonpath='{.spec.volumeName}'

# 2. In Azure Portal, find the managed disk and restore from snapshot
# 3. Create a new disk from snapshot
# 4. Create a new PVC pointing to the restored disk
# 5. Delete the old pod — the new pod will bind to the restored PVC
```

### Schedule Regular Backups

Add a cron job to take daily database backups:

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: postgresql-backup
  namespace: circleguard-dev
spec:
  schedule: "0 2 * * *"  # Daily at 2am
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: pgdump
            image: postgres:16-alpine
            command:
            - sh
            - -c
            - pg_dump -h postgresql-service -U admin -d circleguard | gzip > /backups/circleguard-$(date +%Y%m%d).sql.gz
            env:
            - name: PGPASSWORD
              value: "password"
          restartPolicy: OnFailure
```

## 4. Quick Reference

| Scenario | Command |
|----------|---------|
| Revert code | `git revert <hash> && git push` |
| Rollback microservice | `kubectl rollout undo deployment/<name> -n circleguard-prod` |
| Rollback all services | `for svc in ...; do kubectl rollout undo deployment/$svc -n circleguard-prod; done` |
| Restore database | `kubectl exec deployment/postgresql-service -- psql -U admin -d circleguard < backup.sql` |
| Restore previous image | `kubectl set image deployment/<name> <container>=<old-image>:<old-tag> -n circleguard-prod` |

## 5. Rollback Test Procedure

Before every production deployment:
1. Verify rollback command works: `kubectl rollout undo deployment/<name> --dry-run=client`
2. Confirm previous image tag is available in GHCR
3. Ensure database backup exists (check PVC snapshot or pg_dump file)
