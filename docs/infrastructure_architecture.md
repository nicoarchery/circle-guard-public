# Arquitectura de Infraestructura: CircleGuard (Azure)

Este documento describe la arquitectura de infraestructura implementada con Terraform para cumplir con el Requisito 2 del Proyecto Final.

## Diagrama de Arquitectura (Mermaid)

```mermaid
graph TD
    subgraph "Azure Subscription"
        subgraph "rg-terraform-state"
            S3[Storage Account: tfstate]
        end

        subgraph "circleguard-rg (Environment: dev/stage/prod)"
            VNET[Virtual Network 10.0.0.0/16]
            Subnet[Subnet: aks-subnet 10.0.1.0/24]
            AKS[AKS Cluster: circleguard-aks]
            Node[Node Pool: Standard_B2ps_v2]
            DB[PostgreSQL Flexible Server]
            
            VNET --> Subnet
            Subnet --> AKS
            AKS --> Node
            AKS -.-> DB
        end
    end

    TF[Terraform CLI] --> S3
    TF --> VNET
```

## Componentes
- **VNET**: Red aislada para seguridad.
- **AKS**: Orquestador de Kubernetes (Tier Free para ahorro de costos).
- **Flex Server PostgreSQL**: Base de datos administrada.
- **Remote Backend**: Estado persistente en Azure Storage con bloqueo de estado.

## Estimación de Costos Mensuales (Aprox)

| Recurso Azure | Cantidad | Costo Unitario (Est.) | Total Mensual |
|:---|:---:|:---|:---|
| **AKS Cluster (B2ps_v2)** | 1 | $15.00 | $15.00 |
| **PostgreSQL Flex Server** | 1 | $12.00 | $12.00 |
| **Storage Account (tfstate)** | 1 | $1.00 | $1.00 |
| **VNET & Public IPs** | 1 | $2.00 | $2.00 |
| **TOTAL ESTIMADO** | | | **$30.00 / Mes** |

> [!NOTE]
> Se utiliza el **Tier Free** de AKS y tamaños de instancia **B-Series** para minimizar el impacto en los créditos de estudiante.

## Estructura de Carpetas
- `modules/`: Módulos reutilizables de infraestructura.
- `environments/`: Configuraciones específicas por ambiente (dev, stage, prod).
