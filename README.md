# 🛡️ CircleGuard Monorepo

**Absolute Privacy. High-Speed Containment. Secure Campus.**

CircleGuard is a state-of-the-art university contact tracing and fencing system designed to identify interconnected contact groups ("Circles") and apply rapid health fences while preserving individual anonymity.

---

## 🌟 Vision & Mission

Our vision is a university campus where health containment speed outpaces lab confirmation timelines without compromising student privacy. CircleGuard leverages campus-native intelligence—class schedules and WiFi infrastructure—to deliver a human-validated, graph-based protection ecosystem.

### Key Differentiators
- **Privacy-as-Code**: Zero real-name exposure outside a secure Health Center vault.
- **Recursive Containment**: Status promotion cascades (Suspect → Probable → Confirmed) that trigger in milliseconds.
- **Campus Integration**: Smart check-ins using existing WiFi AP triangulation and Bluetooth Low Energy (BLE).

---

## 📊 Success Metrics

| Metric | Target | Measurement |
|:---|:---|:---|
| **Containment Speed** | < 60 Seconds | Automated test of promotion engine cascade |
| **Privacy Compliance** | 100% Anonymity | Penetration test on graph database (Zero real names) |
| **Check-in Adoption** | > 70% | Analytics on scheduled class contact validation |
| **False Positive Rate** | < 15% | Post-fence surveys of actual vs. suspected contact |
| **System Uptime** | 99.5% | 7:00 AM – 10:00 PM (Academic Peak Hours) |

---

## 🏗️ Architecture Overview

CircleGuard follows a **Microservice Architecture** built on a **Hybrid Data Model**.

### Core Engine
1. **Status Promotion Machine**: Uses **Neo4j** for recursive graph traversals to identify contacts within a 14-day temporal window.
2. **Anonymization Vault**: A segregated **PostgreSQL** vault handles salted-hash identity mapping, compliant with **FERPA** regulations.
3. **Event-Driven Core**: **Apache Kafka** manages asynchronous status changes, audit logs, and notification dispatches.

### Services Directory
- **Auth Service**: Dual-chain LDAP (University) / Local (Guest) auth with Dynamic RBAC.
- **Identity Service**: Cryptographic vault for anonymizing real identities.
- **Promotion Service**: The status engine (Recursive Graph Processing).
- **Notification Service**: Multi-channel dispatcher (Push/Email/SMS).
- **Form Service**: Dynamic health questionnaire engine.
- **Gateway Service**: Campus entry validation via signed, time-limited QR tokens.
- **Dashboard Service**: Geospatial hotspot analytics (Privacy-preserving).
- **File Service**: Secure certificate and document storage (S3-compatible).

---

## 🛠️ Technical Stack

| Layer | Technology | Rationale |
|:---|:---|:---|
| **Backend** | Spring Boot 4 / Java 21 | Enterprise-grade maturity & low-latency Jakarta EE support. |
| **Graph DB** | Neo4j 5.26 | High-performance recursive traversals unreachable with SQL. |
| **Relational DB**| PostgreSQL 16 | ACID compliant storage for identity and configuration. |
| **Message Bus** | Apache Kafka 7.6 | Persistent, audit-trailed event log for status dispatches. |
| **Caching** | Redis 7.2 | L2 distributed cache for rapid entry-gate status validation. |
| **Mobile/Web** | Expo (React Native) | Unified codebase across iOS, Android, and Browser. |
| **Infra** | Kubernetes | Orchestration for high availability and auto-scaling. |

---

## 🗺️ Roadmap

### Phase 1: MVP — The Intelligence Core (Current)
- [x] Status Promotion Machine (Suspect → Probable → Confirmed).
- [x] Temporal graph with 14-day TTL edges.
- [x] Multi-channel fence notifications (Push/Email/SMS).
- [ ] Health Center de-identification console.

### Phase 2: Growth — Spatial Intelligence
- [ ] WiFi AP triangulation integration.
- [ ] Campus entry validation (Gatekeeper) QR integration.
- [ ] LMS integration for "Remote Attendance" status automation.

### Phase 3: Vision — Full Ecosystem
- [ ] Off-campus circle detection via P2P Bluetooth.
- [ ] Global Health Dashboard with hotspot visualization.
- [ ] Lab API bridge for automated test result ingestion.

---

---

## 📈 Estado del Proyecto (Cumplimiento)

Actualmente, el proyecto cumple con los siguientes requisitos del curso:

| Requisito | Estado | Evidencia Principal |
|:---|:---:|:---|
| **1. Metodología Ágil** | ✅ 100% | `docs/METODOLOGIA_AGIL.md` |
| **2. Infraestructura (IaC)**| ✅ 100% | Carpeta `terraform/` (Dev, Stage, Prod validados) |
| **3. Patrones de Diseño** | ✅ 100% | `docs/PATRONES_DISENO.md` e implementación en `PromotionService` |
| **4. CI/CD Avanzado** | 🚧 20% | Jenkinsfile base (En proceso de integración con Azure) |
| **5. Pruebas Completas** | 🚧 30% | Pruebas unitarias/integración de los microservicios |

---

## 🧪 Guía de Pruebas Rápidas

### 1. Verificar Infraestructura (Azure)
Para validar que el código de infraestructura funciona y es modular:
```bash
cd terraform/environments/dev
terraform init
terraform plan # Esto mostrará que está listo para crear 14+ recursos en Azure
```

### 2. Verificar Patrones de Diseño (Resiliencia)
Hemos implementado **Circuit Breaker** y **Retry** en el servicio de Promoción para el proceso de limpieza de estados automáticos:
- **Ubicación**: `services/circleguard-promotion-service/src/main/java/com/circleguard/promotion/service/StatusLifecycleService.java`
- **Prueba**: Busca las anotaciones `@CircuitBreaker` y el método `fallbackStatusCleanup`. Si el broker falla, verás en los logs: `"Circuit breaker 'statusCleanup' opened!"`.

---

## 💻 Local Development

### 1. Infrastructure
Ensure Docker is installed, then start the middleware stack:
```bash
docker-compose -f docker-compose.dev.yml up -d
```
*Middleware includes: PostgreSQL, Neo4j, Kafka, Zookeeper, Redis, and OpenLDAP.*

### 2. Build & Run
CircleGuard uses Gradle for parallel builds across services:
```bash
# Start all microservices in parallel
./gradlew bootRun --parallel

# Start a specific service
./gradlew :services:<service-name>:bootRun
```

### 3. API Exploration
Every service exposes an OpenAPI 3.0 interface. Once running, visit:
`http://localhost:<service-port>/swagger-ui/index.html`

---

## 📱 Frontend Development

The frontend is built using **Expo (React Native)**, supporting iOS, Android, and Web from a single codebase located in `/mobile`.

### 1. Prerequisites
Ensure you have Node.js installed and dependencies loaded:
```bash
cd mobile
npm install
```

### 2. Run the Application
You can run the app in various modes depending on your target platform:

| Platform | Command | Notes |
|:---|:---|:---|
| **Development Menu** | `npm run start` | Opens the Expo Go start-up menu. |
| **Android** | `npm run android` | Requires Android Studio / Emulator or a connected device. |
| **iOS** | `npm run ios` | Requires macOS with Xcode / Simulator installed. |
| **Web Browser** | `npm run web` | Launches the dashboard/app in your default browser. |

### 3. Testing
To run frontend unit and component tests:
```bash
npm run test
```

---

## 🧪 Testing

We maintain high system integrity via multi-level testing:

| Command | Scope |
|:---|:---|
| `./gradlew test` | Full system suite (Unit + Integration) |
| `./gradlew :services:<name>:test` | Single service testing |

**Note**: Integration tests use **Testcontainers** to spawn ephemeral Neo4j and PostgreSQL instances for zero-side-effect validation.

---

## 🚀 Gestión de Infraestructura (Azure)

Sigue estos pasos para trabajar con la infraestructura en la nube sin agotar tus créditos.

### 1. La variable de sesión (¡Muy importante!)
Cada vez que abras una nueva terminal, debes asegurarte de que las credenciales estén cargadas.
**Verificación:** `echo $ARM_CLIENT_ID`. Si está vacío, ejecuta:
```bash
export ARM_CLIENT_ID="tu-appId"
export ARM_CLIENT_SECRET="tu-password"
export ARM_TENANT_ID="tu-tenant"
export ARM_SUBSCRIPTION_ID="tu-subscriptionId"
```

### 2. Estructura de carpetas
Ejecuta siempre los comandos de Terraform desde la carpeta del ambiente:
```bash
cd terraform/environments/dev
```

### 3. Ciclo de Vida (Interruptor de ahorro)
- **Para encender (Desplegar):**
  ```bash
  terraform init
  terraform apply -auto-approve
  ```
- **Para apagar (Ahorro total):**
  ```bash
  terraform destroy -auto-approve
  ```

---

## 🔐 Privacy & Compliance

- **FERPA Compliance**: Student identities are never stored in the contact graph.
- **Right to be Forgotten**: Users can trigger complete data purging via the Identity Vault.
- **Temporal Privacy**: All contact edges are automatically purged after 14 days.
