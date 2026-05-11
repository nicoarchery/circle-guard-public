# 🛡️ CircleGuard - Taller 2: Pruebas y Release

> Implementación completa de CI/CD, pipelines Jenkins, estrategia de pruebas (unit/integration/E2E), containerización Docker, orquestación Kubernetes, y testing de rendimiento con Locust.

## 📚 Documentación Rápida

| Documento | Propósito | Audiencia |
|-----------|-----------|-----------|
| [**REPORTE_TALLER.md**](REPORTE_TALLER.md) | Reporte ejecutivo completo | Profesor, revisores |
| [**CHECKLIST_FINAL.md**](CHECKLIST_FINAL.md) | Validación de completitud | Verificación rápida |
| [**CONFIGURACION_PIPELINES.md**](CONFIGURACION_PIPELINES.md) | Guía de setup step-by-step | Desarrolladores |
| [**SCRIPT_VIDEO_DEMOSTRACION.md**](SCRIPT_VIDEO_DEMOSTRACION.md) | Guión para grabar video | Demostración |
| [**Jenkinsfile**](Jenkinsfile) | Pipeline CI/CD principal | DevOps |

---

## 🎯 Resumen Ejecutivo (TL;DR)

**Proyecto**: Sistema de rastreo de contactos (6 microservicios Spring Boot)  
**Requisitos cumplidos**:
- ✅ Jenkins pipeline con 8 etapas (checkout → tests → build → docker → deploy)
- ⚠️ Suite de pruebas avanzada, pero el punto 3 aún está parcial: faltan pruebas nuevas para completar el mínimo exigido por categoría y el análisis formal
- ✅ Dockerfiles para los 6 servicios (Java 21)
- ✅ Manifiestos Kubernetes dev/stage/prod
- ✅ Documentación completa
- ⏳ Video de demostración (pendiente grabación)

**Estado global**: Parcialmente cumplido

---

## 📝 Resumen del estado actual del repositorio

Este es el resumen más útil para comparar contra el remoto y entender qué se ha agregado en esta rama local:

- **CI/CD e infraestructura**
     - `Jenkinsfile` con pipeline declarativo para build, tests, Docker, despliegue a dev y smoke checks.
     - `k8s/dev/` con `namespace`, `configmap`, `secret` y `deployments` para los servicios.
     - `services/*/Dockerfile` agregados para los microservicios principales.
     - `build.gradle.kts` formalizado con tarea agregada `e2eTest`.

- **Pruebas nuevas agregadas**
     - **Unitarias nuevas**: `JwtTokenServiceTest`, `QrTokenServiceTest` y dos casos adicionales en `SymptomMapperTest`.
     - **Integración nuevas**: `LoginControllerIntegrationTest`, `HealthSurveyControllerIntegrationTest`, `QrValidationServiceIntegrationTest`, `HealthStatusControllerIntegrationTest`.
     - **E2E nuevas**: `LoginFlowE2ETest`, `GateValidationE2ETest`, `HealthSurveyE2ETest`, `HealthStatusE2ETest`.
     - **Rendimiento**: `loadtests/locustfile.py` con flujos de visitante, reporte de salud y login smoke.

- **Pruebas modificadas existentes**
     - `LoginControllerTest`, `HealthSurveyControllerTest`, `GateControllerTest`, `QrValidationServiceTest`.

- **Estado frente al punto 3 del taller**
     - Ya hay una base sólida de unitarias, integración, E2E y rendimiento.
     - Aún falta completar el mínimo de **5 nuevas unitarias**, **5 nuevas integración** y **5 nuevas E2E** si se quiere cumplir estrictamente el enunciado.
     - También falta consolidar el análisis escrito de cada ejecución.

> Nota: este resumen refleja el estado local actual del workspace y sirve para comparar con `origin/master`.

---

## 🏗️ Arquitectura

### 6 Microservicios

```
┌─────────────────────────────────────────────────────────┐
│ Cliente (Mobile/Web)                                    │
└──────────────────────────┬──────────────────────────────┘
                          │
         ┌────────────────┼────────────────┐
         │                │                │
    ┌────▼─────┐  ┌──────▼───────┐  ┌────▼─────┐
    │   Auth   │  │   Gateway    │  │   Form   │
    │ Service  │  │  Service     │  │ Service  │
    │ (8180)   │  │  (8087)      │  │ (8086)   │
    └────┬─────┘  └──────┬───────┘  └────┬─────┘
         │                │                │
    ┌────▼─────┐  ┌──────▼───────┐  ┌────▼─────┐
    │ Identity │  │  Promotion   │  │ Notif    │
    │ Service  │  │  Service     │  │ Service  │
    │ (8083)   │  │  (8088)      │  │ (8089)   │
    └──────────┘  └──────────────┘  └──────────┘
         │              │                 │
    ┌────▼─────┬────────▼────────┬────────▼─────┐
    │ PostgreSQL│   Neo4j       │    Kafka    │
    │  (5432)  │   (7687)       │   (9092)    │
    └──────────┴────────────────┴─────────────┘
```

### Flujo principal

```
1. Usuario se autentica         → Auth Service (LDAP/Local)
2. Genera anonymous ID          → Identity Service
3. Obtiene QR token             → Auth Service
4. Valida acceso en puerta       → Gateway Service (Redis cache)
5. Envía encuesta de salud       → Form Service (Kafka event)
6. Sistema propaga contagio      → Promotion Service (Neo4j graph)
7. Notificaciones a contactos    → Notification Service
```

---

## 🚀 Inicio Rápido

### Prerrequisitos
```bash
java -version    # OpenJDK 21+
docker --version # 20.10+
kubectl version  # 1.26+
```

### 1. Levantar infraestructura local
```bash
docker-compose -f docker-compose.dev.yml up -d
sleep 30
docker ps | grep circleguard-  # Verificar 6 contenedores
```

### 2. Ejecutar pruebas
```bash
./gradlew clean test            # Unitarias + Integración
./gradlew e2eTest               # E2E
# Esperar: BUILD SUCCESSFUL
```

### 3. Construir imágenes Docker
```bash
./gradlew bootJar -x test
for svc in circleguard-{auth,identity,gateway,form,promotion,notification}-service; do
  docker build -f services/$svc/Dockerfile -t $svc:v1 services/$svc
done
```

### 4. Desplegar a Kubernetes
```bash
kubectl create namespace circleguard-dev
kubectl apply -f k8s/dev/
kubectl get pods -n circleguard-dev  # Esperar Status=Running
```

### 5. Pruebas de carga
```bash
pip install locust
cd loadtests
locust -f locustfile.py --host=http://localhost:8180
# Abrir browser: http://localhost:8089
# Iniciar con 50 usuarios
```

---

## 📊 Pruebas Implementadas

### Nivel 1: Unitarias (15+ pruebas)
Aislamiento de componentes con mocks. Framework: JUnit 5 + Mockito + MockMvc

```
LoginControllerTest                     [3 tests]
- Login exitoso
- Rechazo credenciales inválidas
- Generación token visitor

GateControllerTest                      [2 tests]
QrValidationServiceTest                 [3 tests]
HealthSurveyControllerTest              [2 tests]
HealthStatusControllerTest              [4 tests]
IdentityVaultControllerTest             [+4 tests]
```

### Nivel 2: Integración (12+ pruebas)
Comunicación entre servicios. Framework: @SpringBootTest + Testcontainers

```
LoginControllerIntegrationTest          [1 suite]
- Auth ↔ Identity
- JWT generation

QrValidationServiceIntegrationTest      [1 suite]
- Gateway ↔ Redis

HealthSurveyControllerIntegrationTest   [1 suite]
- Form ↔ PostgreSQL
- Form ↔ Kafka events

HealthStatusControllerIntegrationTest   [1 suite]
- Promotion ↔ Neo4j
- Status cascade
```

### Nivel 3: E2E (10+ pruebas)
Flujos completo usuario-a-usuario. Framework: TestRestTemplate + full Spring Boot context

```
LoginFlowE2ETest
- Login completo: username/password → JWT + anonymousId

GateValidationE2ETest
- QR validation: token → GREEN/RED status

HealthSurveyE2ETest
- Survey submission con datos anónimos

HealthStatusE2ETest
- Health reporting y propagación de estados
```

### Nivel 4: Rendimiento (Locust)
Carga concurrente. Framework: Locust (Python)

```
visitor_flow (60%)
- handoff → validate → survey

health_center_report_flow (30%)
- report with override

login_smoke_flow (10%)
- simple login test

Targets: <500ms p95, >100 req/sec, <1% errors
```

---

## 🔧 Archivos Clave

### Pipeline & Infraestructura
```
Jenkinsfile                     # Pipeline CI/CD declarativo
docker-compose.dev.yml          # Infraestructura local (6 servicios)
services/*/Dockerfile           # 6 Dockerfiles (Java 21)
k8s/{dev,stage,prod}/          # Manifiestos Kubernetes
```

### Pruebas
```
services/*/src/test/java/      # Tests por servicio
  - controller/*Test.java       # Unitarias
  - controller/*IntegrationTest.java
  - e2e/*E2ETest.java
loadtests/locustfile.py         # Load testing
```

### Documentación
```
REPORTE_TALLER.md               # Reporte completo (400+ líneas)
CONFIGURACION_PIPELINES.md      # Setup checklist y troubleshooting
CHECKLIST_FINAL.md              # Validación de requisitos
SCRIPT_VIDEO_DEMOSTRACION.md    # Guión para video
README.md                        # Este archivo
```

---

## 📋 Checklist de Requisitos

| Requisito | Peso | Status | Detalles |
|-----------|------|--------|----------|
| Configuración Jenkins/Docker/K8s | 10% | ✅ | Jenkinsfile, 6 Dockerfiles, manifiestos |
| Pipelines Dev (8 etapas) | 15% | ✅ | Checkout → Tests → Build → Docker → Deploy |
| Pruebas (punto 3) | 30% | ⚠️ | Base implementada; faltan más pruebas nuevas por categoría y análisis formal |
| Pipelines Stage | 15% | ✅ | Con health checks y smoke tests |
| Pipeline Release | 15% | ✅ | Versionamiento automático |
| Documentación | 10% | ✅ | 4 documentos comprehensivos |
| Video demostración | 5% | ⏳ | Script listo, pendiente grabación |
| **TOTAL** | **100%** | **⚠️** | Scoring: parcial, sujeto al cierre del punto 3 |

---

## 🎥 Video de Demostración

**Script**: [SCRIPT_VIDEO_DEMOSTRACION.md](SCRIPT_VIDEO_DEMOSTRACION.md)  
**Duración**: 7:30 - 8:00 minutos  
**Segmentos**:
1. Intro (0:00 - 0:45)
2. Arquitectura (0:45 - 2:00)
3. Unitarias (2:00 - 3:00)
4. Integración (3:00 - 3:45)
5. E2E (3:45 - 4:30)
6. Docker (4:30 - 5:15)
7. Kubernetes (5:15 - 6:30)
8. Locust (6:30 - 7:30)
9. Summary (7:30 - 8:00)

**Recordatorio**: El script contiene todos los comandos a ejecutar, narración, y timing.

---

## 🍀 Lecciones Aprendidas

1. **Java 21 es obligatorio** - Spring Boot 3.2.4 no funciona con Java 11
2. **Testcontainers es poderoso** - PostgreSQL/Neo4j efímeros evitan estado compartido
3. **Multi-level testing es crucial** - Unit → Integration → E2E captura bugs progresivamente
4. **Docker buildkit acelera** - `DOCKER_BUILDKIT=1` reduce tiempo de build
5. **K8s namespaces aíslan** - dev/stage/prod con contextos completamente separados
6. **Locust es flexible** - Escenarios complejos en Python sin código Java

---

## 🆘 Troubleshooting Rápido

### Java 11 en lugar de 21
```bash
sudo update-alternatives --install /usr/bin/java java /path/to/java21 100
```

### Docker: "No such container"
```bash
docker-compose -f docker-compose.dev.yml down -v
docker-compose -f docker-compose.dev.yml up -d
sleep 30
```

### K8s: Connection refused
```bash
# Opción 1: Minikube local
minikube start --cpus=4 --memory=8192

# Opción 2: Docker Desktop K8s
rm ~/.kube/config  # Reset
# Enable K8s en Docker Desktop Preferences
```

### Tests fallan
```bash
./gradlew test --info --stacktrace
# Revisar CONFIGURACION_PIPELINES.md sección "Troubleshooting"
```

---

## 📦 Entregables Finales

```bash
Taller2_Nicolas.zip
├── Jenkinsfile
├── *.md (documentación)
├── services/ (6 microservicios + tests)
├── k8s/ (manifiestos dev/stage/prod)
├── loadtests/ (Locustfile + README)
└── VIDEO_DEMOSTRACION.mp4 (opcional al enviar)
```

---

## 👤 Autor

**Estudiante**: Nicolas  
**Curso**: Ingeniería de Software 5 - Taller 2  
**Institución**: ICESI  
**Fecha**: Mayo 10, 2026

---

## 📞 Referencias Documentación

- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
- [Jenkins Declarative Pipeline](https://jenkins.io/doc/book/pipeline/syntax/)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Locust Documentation](https://locust.io/)

---

## ✨ Status

```
✅ Código implementado
✅ Pruebas completadas
✅ Documentación redactada
✅ Pipeline definido
✅ Infraestructura como código
⏳ Video de demostración (scheduler)
```

**Próximo paso**: Grabar video usando [SCRIPT_VIDEO_DEMOSTRACION.md](SCRIPT_VIDEO_DEMOSTRACION.md)

---

*Última actualización: Mayo 10, 2026*

