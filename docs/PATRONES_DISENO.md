# Documentación de Patrones de Diseño - CircleGuard

Este documento detalla los patrones de diseño implementados en la arquitectura para cumplir con los requisitos de escalabilidad, resiliencia y configuración dinámica.

## 1. Patrones de Diseño Estructurales

### 1.1 Repository Pattern (Implementado ✅)
- **Ubicación**: `com.circleguard.promotion.repository`
- **Propósito**: Abstraer el acceso a los datos de las bases de datos (PostgreSQL y Neo4j), permitiendo que la lógica de negocio permanezca agnóstica a la tecnología de persistencia.
- **Beneficio**: Facilita el intercambio de bases de datos y mejora la testabilidad mediante mocks.

### 1.2 Data Transfer Object - DTO (Implementado ✅)
- **Ubicación**: `com.circleguard.promotion.dto`
- **Propósito**: Encapsular los datos que se transfieren entre microservicios o hacia el frontend.
- **Beneficio**: Protege el modelo interno (Entidades JPA/Neo4j) y optimiza el payload de red.

---

## 2. Patrones de Resiliencia (Nuevos/Adicionales)

### 2.1 Circuit Breaker - Resilience4j (Implementado ✅)
- **Ubicación**: `StatusLifecycleService.java` (Anotación `@CircuitBreaker`)
- **Propósito**: Detener las peticiones a un sistema que está fallando (ej: Neo4j) para evitar bloqueos de hilos y fallos en cascada.
- **Beneficio**: Mejora la **Tolerancia a Fallos**. Si la base de datos falla, el sistema no se queda colgado; en su lugar, ejecuta un `fallback` controlado.

### 2.2 Retry Pattern (Implementado ✅)
- **Ubicación**: `StatusLifecycleService.java` (Anotación `@Retry`)
- **Propósito**: Reintentar automáticamente operaciones transitorias que fallaron inicialmente.
- **Beneficio**: Maneja fallos temporales de red sin necesidad de intervención manual o error al usuario.

---

## 3. Patrones de Configuración y Mensajería

### 3.1 Feature Toggle Pattern (Implementado ✅)
- **Ubicación**: `StatusLifecycleService.java` (`autoCleanupEnabled`)
- **Propósito**: Permitir la activación o desactivación de funcionalidades lógicas (limpieza automática) mediante configuración externa sin recompilar.
- **Beneficio**: **Flexibilidad Operativa**. Permite apagar procesos pesados en momentos de carga crítica en producción.

### 3.2 External Configuration (Implementado ✅)
- **Ubicación**: `application.yml` y variables de entorno de Azure/K8s.
- **Propósito**: Separar completamente los parámetros de infraestructura del código fuente.
- **Beneficio**: Mismo binario desplegable en múltiples entornos (Dev, Stage, Prod).

---

## 🧪 Cómo Validar los Patrones

### Probar Circuit Breaker & Retry
Ejecutar el test de verificación de resiliencia:
```bash
./gradlew :services:circleguard-promotion-service:test --tests ResilienceVerificationTest
```
Si Neo4j falla, verás en los logs: `Circuit breaker 'statusCleanup' opened!`.

### Probar Feature Toggle
Cambiar en `application.yml`:
```yaml
circleguard:
  features:
    auto-cleanup-enabled: false
```
Al correr el servicio, verás el log: `Status lifecycle check skipped: Feature 'auto-cleanup-enabled' is DISABLED`.
