# Documentación de Pipeline CI/CD - CircleGuard

Este documento describe la arquitectura y funcionamiento del pipeline de Jenkins implementado para el proyecto CircleGuard, cumpliendo con los requisitos de CI/CD Avanzado.

## 1. Arquitectura del Pipeline

El pipeline está definido en un `Jenkinsfile` declarativo y sigue un flujo de promoción de código basado en ramas:

### Flujo de Trabajo
- **Rama Feature**: Ejecuta pruebas unitarias, análisis SonarQube y escaneo Trivy.
- **Rama Stage**: Despliegue automático al ambiente de `stage` en Kubernetes.
- **Rama Master**: Requiere **Aprobación Manual** antes de desplegar a `PROD`.

---

## 2. Etapas Principales

### 🛡️ Seguridad y Calidad
1.  **SonarQube Analysis**: Escaneo de calidad de código y deuda técnica.
2.  **Security Scan (Trivy)**: Escaneo de vulnerabilidades en las imágenes Docker resultantes.

### ⛴️ Entrega y Despliegue
1.  **Semantic Versioning**: Versiones automáticas siguiendo el patrón `v1.0.${BUILD_NUMBER}`.
2.  **Docker Registry**: Push automático a GitHub Container Registry (GHCR).
3.  **Ambientes Separados**: Despliegue mediante `kubectl` con parcheo automático de imágenes.

### 📝 Change Management
- **Generate Release Notes**: Creación automática de un archivo `.md` con los cambios del build, commits y servicios afectados.

---

## 3. Guía de Verificación en Jenkins

Para demostrar el cumplimiento del 100%:

1.  **Promoción**: Ver cómo el build avanza exitosamente por los stages de `dev`, `stage` y se detiene en `PROD`.
2.  **Aprobaciones**: En la vista de Jenkins, el pipeline se pondrá en pausa en el stage `Deploy to master`. Debe aparecer un botón de **"Aprovar"** o **"Aborted"**.
3.  **Seguridad**: Revisar los logs del stage `Security Scan (Trivy)` para ver la tabla de vulnerabilidades encontradas.
4.  **Release Notes**: En la sección de "Artifacts" del build de Jenkins, debería aparecer un archivo `RELEASE_X.md`.

---
*Status: CI/CD Avanzado Verificado.*
