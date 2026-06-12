# CircleGuard — Manual de Operaciones

## 1. Arquitectura General

```
┌─────────────────────────────────────────────────────────┐
│                     AKS Cluster                          │
│              aks-circleguard-dev (1 nodo)                │
│          Standard_B4pls_v2 (4 vCPU, ~8GB RAM)            │
│                                                          │
│  ┌──────────────────────────────────────────────────┐    │
│  │              Namespace: circleguard-dev            │    │
│  │                                                    │    │
│  │  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌───────┐   │    │
│  │  │ auth │ │identity│gateway│ │ form │ │promo  │   │    │
│  │  └──────┘ └──────┘ └──────┘ └──────┘ └───────┘   │    │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐           │    │
│  │  │notific.  │ │dashboard │ │   file   │  (no k8s) │    │
│  │  └──────────┘ └──────────┘ └──────────┘           │    │
│  │                                                    │    │
│  │  ┌──────┐ ┌─────┐ ┌─────┐ ┌──────┐ ┌──────┐     │    │
│  │  │Postgre│ │Redis│ │Kafka│ │  ZK  │ │ LDAP │     │    │
│  │  └──────┘ └─────┘ └─────┘ └──────┘ └──────┘     │    │
│  │  ┌──────┐ ┌──────┐ ┌──────────────────┐          │    │
│  │  │Neo4j │ │Elastic│ │ Prom+Graf+Alert  │          │    │
│  │  └──────┘ └──────┘ └──────────────────┘          │    │
│  │  ┌──────────────────────────────────┐             │    │
│  │  │    ELK (Filebeat+Logstash+Kibana)│             │    │
│  │  └──────────────────────────────────┘             │    │
│  └──────────────────────────────────────────────────┘    │
│                                                          │
│  Todos los servicios: ClusterIP                          │
│  Acceso externo: port-forward                            │
└─────────────────────────────────────────────────────────┘
```

### Microservicios (8)
| Servicio | Puerto Cluster | Puerto Local | Health Check |
|---|---|---|---|
| auth | 8080 | 8180 | /actuator/health |
| identity | 8080 | 8083 | /actuator/health |
| gateway | 8080 | 8087 | /actuator/health |
| form | 8080 | 8086 | /actuator/health |
| promotion | 8080 | 8088 | /actuator/health |
| notification | 8080 | 8082 | /actuator/health |
| dashboard | — | — | No desplegado en k8s |
| file | — | — | No desplegado en k8s |

### Infraestructura
| Servicio | Puerto | Propósito |
|---|---|---|
| PostgreSQL | 5432 | Base de datos principal |
| Redis | 6379 | Caché |
| Kafka | 9092 | Mensajería |
| Zookeeper | 2181 | Coordinación Kafka |
| LDAP | 389 | Directorio |
| Neo4j | 7687 | Grafos |
| Elasticsearch | 9200 | Búsqueda/logs |
| Prometheus | 9090 | Métricas |
| Grafana | 3000 | Dashboards |
| Alertmanager | 9093 | Alertas |
| Kibana | 5601 | Visualización ELK |
| Logstash | 5000 | Ingesta logs |
| Jaeger | 16686 | Tracing (escalado a 0) |

---

## 2. Prerrequisitos

- `kubectl` — conectado al clúster `aks-circleguard-dev`
- `docker` — para builds locales
- `node` >= 18, `npm`, `npx` — para app móvil/web
- `java` 21 — para builds Gradle locales
- `curl` — para verificar endpoints
- Acceso a `ghcr.io/nicoarchery/circleguard/*` — imágenes Docker

---

## 3. Inicio Rápido

```bash
# Terminal 1: exponer servicios de AKS a localhost
bash scripts/port-forward.sh

# Terminal 2: iniciar app móvil/web
npx expo start --web

# Verificar que los servicios responden
for p in 8180 8083 8087 8086 8088 8082; do
  curl -sf "http://localhost:$p/actuator/health" >/dev/null \
    && echo "[OK] localhost:$p" \
    || echo "[--] localhost:$p"
done
```

---

## 4. Credenciales

### Usuarios de prueba (base de datos)
| Usuario | Contraseña | Roles |
|---|---|---|
| staff_guard | password | GATE_STAFF |
| health_user | password | HEALTH_CENTER |
| super_admin | password | GATE_STAFF, HEALTH_CENTER, SUPER_ADMIN |

### Jenkins
- URL: `http://localhost:8080`
- CI/CD pipeline: `CircleGuard Pipeline`

### GitHub
- Repo: `github.com/nicoarchery/circle-guard-public`
- Container Registry: `ghcr.io/nicoarchery/circleguard/*`

---

## 5. Base de Datos

| Database | Propósito |
|---|---|
| auth_db | Autenticación y usuarios |
| identity_db | Identidades de visitantes |
| gateway_db | Validación de QR/gate |
| form_db | Cuestionarios |
| promotion_db | Promociones |
| notification_db | Notificaciones |

- **Usuario**: `circleguard`
- **Motor**: PostgreSQL (en AKS)
- **DDL**: Hibernate `ddl-auto: update` (Flyway deshabilitado)
- **PVC**: 1Gi, montado en `/var/lib/postgresql/data/pgdata`

```bash
# Conectarse a la base de datos auth
kubectl exec -n circleguard-dev deploy/postgresql-service -- \
  psql -U circleguard -d auth_db

# Consultar usuarios
kubectl exec -n circleguard-dev deploy/postgresql-service -- \
  psql -U circleguard -d auth_db -c "SELECT username, role_names FROM local_users;"

# Conectarse a otras bases
kubectl exec -n circleguard-dev deploy/postgresql-service -- \
  psql -U circleguard -d gateway_db
```

---

## 6. CI/CD

- **Pipeline**: Jenkins declarativo (`Jenkinsfile`)
- **Imágenes**: `ghcr.io/nicoarchery/circleguard/<service>:v1.0.${BUILD_NUMBER}`
- **Tags Git**: `v1.0.${BUILD_NUMBER}` en cada release exitoso
- **Release notes**: se generan automáticamente y se commitean a `build/release-notes/`
- **Ambientes**: dev → stage → prod (promoción)
- **Documentación relacionada**:
  - [CICD_PIPELINE.md](CICD_PIPELINE.md) — detalle del pipeline
  - [CHANGE_MANAGEMENT.md](CHANGE_MANAGEMENT.md) — proceso de cambios
  - [ROLLBACK_PLAN.md](ROLLBACK_PLAN.md) — procedimientos de rollback

---

## 7. Comandos Útiles

### Pods
```bash
# Todos los pods
kubectl get pods -n circleguard-dev -o wide

# Solo microservicios
kubectl get pods -n circleguard-dev -l 'app in (circleguard-auth-service,circleguard-identity-service,circleguard-gateway-service,circleguard-form-service,circleguard-promotion-service,circleguard-notification-service)'

# Pods con problemas
kubectl get pods -n circleguard-dev --field-selector status.phase!=Running

# Logs de un servicio específico
kubectl logs -n circleguard-dev -l app=circleguard-auth-service --tail=50

# Describir pod (para ver eventos/errores)
kubectl describe pod -n circleguard-dev -l app=circleguard-auth-service
```

### Services
```bash
# Todos los servicios ClusterIP
kubectl get svc -n circleguard-dev

# Service específico
kubectl get svc circleguard-auth-service -n circleguard-dev -o yaml
```

### Port-Forward
```bash
# Forward manual de un servicio específico
kubectl port-forward svc/circleguard-auth-service 8180:8080 -n circleguard-dev

# Detener todos los forwards
bash scripts/stop-port-forward.sh

# Reiniciar forwards
bash scripts/stop-port-forward.sh && bash scripts/port-forward.sh
```

### Base de datos
```bash
# Listar databases
kubectl exec -n circleguard-dev deploy/postgresql-service -- \
  psql -U circleguard -d postgres -c "\l"

# Ejecutar consulta directa
kubectl exec -n circleguard-dev deploy/postgresql-service -- \
  psql -U circleguard -d auth_db -c "SELECT * FROM local_users;"

# Backup rápido de una tabla
kubectl exec -n circleguard-dev deploy/postgresql-service -- \
  pg_dump -U circleguard -d auth_db --table=local_users --data-only
```

### Cluster
```bash
# Información del clúster
kubectl cluster-info

# Nodos
kubectl get nodes -o wide

# Recursos del nodo
kubectl describe node aks-b4pool-16560098-vmss000000 | grep -A5 "Capacity"
```

---

## 8. Troubleshooting

### Pod en estado Pending
Causa más probable: recursos insuficientes (nodo único con 4 vCPU, ~8GB).
```bash
# Ver eventos del pod
kubectl describe pod <pod-name> -n circleguard-dev | grep -A10 Events

# Ver recursos usados
kubectl top pod -n circleguard-dev
```

### Servicio no responde en port-forward
```bash
# Verificar que el pod está Running
kubectl get pods -n circleguard-dev -l app=<service>

# Verificar que el forward sigue vivo
ps aux | grep "port-forward"

# Verificar health directo desde el cluster
kubectl run -it --rm test --image=curlimages/curl -n circleguard-dev -- \
  curl -s http://circleguard-auth-service:8080/actuator/health
```

### Login falla
- Verificar que el seed de base de datos se ejecutó
- Confirmar que el hash BCrypt corresponde a "password"
- Ver logs del servicio auth: `kubectl logs -n circleguard-dev -l app=circleguard-auth-service`

### Error "Connection refused to jaeger:4318"
No es crítico. Jaeger está escalado a 0 para ahorrar recursos. El tracing falla silenciosamente.

### PostgreSQL no inicia
- Verificar PVC: `kubectl get pvc -n circleguard-dev`
- Verificar que el subdirectorio `pgdata` no tenga contenidos extraños
- Logs: `kubectl logs -n circleguard-dev deploy/postgresql-service`

### Jenkins build falla
- Revisar `localhost:8080` → build → "Console Output"
- Errores comunes: credenciales SonarQube, falta de espacio en Jenkins, errores de Gradle

---

## 9. Monitoreo y Logs

Todos los servicios de monitoreo son ClusterIP — no expuestos externamente.

```bash
# Forward temporal para ver Grafana
kubectl port-forward svc/grafana 3000:3000 -n circleguard-dev
# Abrir: http://localhost:3000

# Forward temporal para ver Prometheus
kubectl port-forward svc/prometheus 9090:9090 -n circleguard-dev

# Forward temporal para ver Kibana
kubectl port-forward svc/kibana 5601:5601 -n circleguard-dev
```

---

## 10. Actualización de Imágenes

Para actualizar la imagen de un servicio sin pasar por Jenkins:

```bash
# Parchear deployment con nueva imagen
kubectl set image deployment/circleguard-auth-service \
  -n circleguard-dev \
  app=ghcr.io/nicoarchery/circleguard/circleguard-auth-service:v1.0.42

# Ver rollout
kubectl rollout status deployment/circleguard-auth-service -n circleguard-dev
```

---

## 11. Referencias

| Documento | Descripción |
|---|---|
| [CHANGE_MANAGEMENT.md](CHANGE_MANAGEMENT.md) | Proceso formal de cambios |
| [ROLLBACK_PLAN.md](ROLLBACK_PLAN.md) | Procedimientos de rollback |
| [CICD_PIPELINE.md](CICD_PIPELINE.md) | Detalle del pipeline Jenkins |
| [infrastructure_architecture.md](infrastructure_architecture.md) | Arquitectura de infraestructura Azure |
| `k8s/dev/` | Manifiestos Kubernetes (dev) |
| `postman/circle-guard-demo.json` | Colección Postman para pruebas |
| `scripts/` | Scripts de operación |
