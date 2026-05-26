# Documentación de Patrones de Diseño - CircleGuard

Este documento identifica los patrones de diseño actuales en la arquitectura y propone la implementación de nuevos patrones para mejorar la resiliencia y configuración del sistema.

## 1. Patrones de Diseño Existentes

### 1.1 Repository Pattern
- **Ubicación**: `com.circleguard.promotion.repository`
- **Descripción**: Se utiliza para desacoplar la lógica de negocio de la persistencia de datos (SQL y NoSQL/Neo4j). Permite cambiar la implementación de la base de datos sin afectar los servicios.

### 1.2 Data Transfer Object (DTO)
- **Ubicación**: `com.circleguard.promotion.dto`
- **Descripción**: Se emplea para estructurar los datos que viajan entre los microservicios y el frontend, evitando exponer directamente las entidades de la base de datos y reduciendo el acoplamiento.

### 1.3 Service Layer Pattern
- **Ubicación**: `com.circleguard.promotion.service`
- **Descripción**: Organiza la lógica de negocio en clases de servicio dedicadas, permitiendo que los controladores solo se encarguen de la gestión de peticiones HTTP.

---

## 2. Nuevos Patrones a Implementar

### 2.1 Circuit Breaker (Resiliencia)
- **Propósito**: Evitar fallos en cascada. Si el servicio de notificaciones falla, el servicio de promoción no debe bloquearse esperando una respuesta.
- **Implementación**: Usaremos **Resilience4j** en el `PromotionService`.

### 2.2 Retry Pattern (Resiliencia)
- **Propósito**: Manejar fallos transitorios en comunicaciones de red o conexiones a bases de datos.
- **Implementación**: Reintentos automáticos configurables con backoff exponencial.

### 2.3 Competing Consumer (Mensajería/Escalabilidad)
- **Propósito**: Permitir que múltiples instancias de un servicio procesen mensajes de una misma cola (Kafka) de forma paralela, aumentando el rendimiento.
- **Implementación**: Se utiliza mediante **Consumer Groups** en Kafka. Al escalar los microservicios en Kubernetes, Kafka reparte automáticamente la carga entre las réplicas.

### 2.4 External Configuration (Configuración)
- **Propósito**: Separar el código de la configuración para permitir cambios sin necesidad de recompilar.
- **Implementación**: Uso de variables de entorno de Azure y perfiles de Spring Boot (`application-dev.yml`).

---

## 3. Beneficios Esperados
- **Alta Disponibilidad**: El sistema seguirá funcionando parcialmente incluso si un servicio externo falla.
- **Mantenibilidad**: Código más limpio y desacoplado siguiendo principios SOLID.
- **Flexibilidad**: Facilidad para desplegar en múltiples nubes cambiando únicamente la configuración externa.
