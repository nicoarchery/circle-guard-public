# Change Management Process

## 1. Change Classification

| Type | Description | Examples | Approval Required |
|------|-------------|----------|-------------------|
| **Emergency** | Critical fix for production outage | Hotfix for security vulnerability, DB corruption | DevOps Lead + emergency meeting |
| **Standard** | Feature, improvement, or bugfix | New API endpoint, UI enhancement | Code review + CI/CD pipeline |
| **Minor** | Configuration or dependency update | Library upgrade, config change, env var | Code review |
| **Routine** | Automated/chore | Dependency bump, CI config, docs | Pipeline auto-approval |

## 2. Change Workflow

```
                   ┌─────────────┐
                   │  Developer   │
                   │  Creates PR  │
                   └──────┬──────┘
                          ▼
                   ┌─────────────┐
                   │ Code Review  │
                   │ (Peer/Tech  │
                   │  Lead)      │
                   └──────┬──────┘
                          ▼
                   ┌─────────────┐
                   │ CI Pipeline  │
                   │ (Tests,     │
                   │  SonarQube, │
                   │  Trivy)     │
                   └──────┬──────┘
                          ▼
              ┌─────────────────────┐
              │ Deploy to Dev (auto) │
              └──────────┬──────────┘
                         ▼
              ┌─────────────────────┐
              │ Deploy to Stage     │
              │  (auto on main)     │
              └──────────┬──────────┘
                         ▼
              ┌─────────────────────┐
              │ Approve Production  │
              │  (Manual in Jenkins)│
              └──────────┬──────────┘
                         ▼
              ┌─────────────────────┐
              │ Deploy to Prod      │
              │  + Git Tag +        │
              │  Release Notes      │
              └─────────────────────┘
```

## 3. Change Request Template

When submitting a change that requires approval (Standard or Emergency):

```yaml
Change ID: CR-YYYY-MM-NNN
Title: <brief description>
Type: [Standard | Emergency | Minor | Routine]
Requester: <name>
Date: <YYYY-MM-DD>

Description:
  <detailed description of what changed and why>

Services Affected:
  - circleguard-auth-service
  - circleguard-identity-service
  - ...

Rollback Plan:
  <link to ROLLBACK_PLAN.md section>

Risk Assessment:
  [Low | Medium | High]

Testing Done:
  - Unit tests
  - Integration tests
  - E2E tests

Approval:
  [ ] Technical Lead
  [ ] DevOps Lead
  [ ] Product Owner
```

## 4. Change Advisory Board (CAB)

For Emergency changes, a mini-CAB consisting of:
- DevOps Lead (final approver)
- Technical Lead of affected service
- QA representative

The CAB can be convened ad-hoc via Slack/Discord for emergency changes.

## 5. Change Windows

| Environment | Window | Approval |
|-------------|--------|----------|
| Dev | Any time | None (auto-deploy) |
| Stage | Business hours (Mon-Fri 8am-6pm) | Merge to main |
| Production | Business hours + approval | Manual Jenkins approval |

Emergency changes to production can be deployed outside business hours with DevOps Lead approval.

## 6. Audit Trail

Every change is tracked via:
1. **Git commit history** — full trace of code changes
2. **Jenkins build log** — pipeline execution record
3. **Release notes** — `build/release-notes/RELEASE_*.md`
4. **Git tags** — `v1.0.*` tags mark production releases
5. **Change Request** — documented in this repo for Standard/Emergency changes
