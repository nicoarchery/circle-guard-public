# Metodología Ágil y Estrategia de Branching

Este documento describe las prácticas ágiles y el flujo de trabajo de Git implementados para el proyecto CircleGuard.

## 1. Metodología Ágil (Scrum)
Se ha adoptado el marco de trabajo **Scrum** para el desarrollo:
- **Herramienta de Gestión**: Jira.
- **Iteraciones**: Sprints de 1 semana.
- **Artefactos**:
  - **Product Backlog**: Lista priorizada de Historias de Usuario (HU).
  - **Sprint Backlog**: Tareas seleccionadas para la iteración actual.
- **Progreso**: Al menos 2 iteraciones (Sprints) han sido completadas satisfactoriamente.

## 2. Estrategia de Branching (GitHub Flow)
Se ha seleccionado **GitHub Flow** por su agilidad y compatibilidad con despliegue continuo de microservicios.

### Flujo de Trabajo:
1.  **main**: La rama principal siempre contiene código estable y desplegable a producción. Queda protegida contra pushes directos.
2.  **feature/nombre-tarea**: Cada nueva funcionalidad o corrección se trabaja en una rama corta de vida.
3.  **Pull Requests (PR)**: Para integrar una rama `feature` a `main`, es obligatorio abrir un PR, pasar las pruebas automáticas en el pipeline y obtener aprobación.
4.  **Merge**: Tras la aprobación, se realiza el merge y se elimina la rama de la característica.

### Ventajas para CircleGuard:
- Permite entregas rápidas de microservicios independientes.
- Minimiza los conflictos de integración comparado con GitFlow tradicional.
