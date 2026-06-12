# CircleGuard — Costo de Infraestructura

> **Nota**: Todos los precios son estimados para región **South Central US**.
> Verificar precios actualizados en [Azure Pricing Calculator](https://azure.microsoft.com/en-us/pricing/calculator/).

---

## 1. Recursos Azure Actuales

| Recurso | Nombre | Detalle |
|---|---|---|
| AKS Cluster | `aks-circleguard-dev` | 1 nodo, Tier Free |
| Nodo | `Standard_B4pls_v2` | ARM64, 4 vCPU, 8 GB RAM |
| Storage Account | `stcircleguardtfstate` | LRS, ~1 GB (tfstate) |
| Virtual Network | `circleguard-vnet` | 10.0.0.0/16 + 1 subnet |
| Managed Disk (OS) | nodo AKS | 30 GB Premium SSD |
| Managed Disk (PVCs) | PostgreSQL 1Gi + Elasticsearch 5Gi | Standard SSD |

---

## 2. Desglose de Costos Mensuales

| Componente | Detalle | Costo Estimado/mes |
|---|---|---|
| **VM Standard_B4pls_v2** | 1 nodo AKS, ARM64, 4 vCPU, 8 GB | ~$61.00 |
| **AKS Tier Free** | Sin costo de plano de control | $0.00 |
| **Storage Account** | LRS, 1 GB | ~$1.00 |
| **Virtual Network** | VNet + IPs | ~$2.00 |
| **Managed Disk OS** | 30 GB Premium SSD | ~$3.00 |
| **Managed Disk PVCs** | 6 GB Standard SSD | ~$1.00 |
| **Load Balancer** | No usado (ClusterIP) | $0.00 |
| **Total estimado** | | **~$68.00 / mes** |

---

## 3. Comparativa: Lo Planeado vs. Lo Real

| Aspecto | Planeado (Terraform) | Real (Desplegado) | Impacto en Costo |
|---|---|---|---|
| VM Size | `Standard_B2ps_v2` (2 vCPU, 4 GB, ~$31/mes) | `Standard_B4pls_v2` (4 vCPU, 8 GB, ~$61/mes) | **+$30/mes** |
| PostgreSQL | Azure Flexible Server (~$25/mes) | StatefulSet en AKS (PVC 1Gi, ~$0.50/mes) | **-$24.50/mes** |
| Ambientes | dev + stage + prod | Solo dev | **No aplica** |
| AKS Tier | Free | Free | $0 |
| Load Balancer | No | No | $0 |

**Costo planeado (Terraform):** ~$59.50/mes
**Costo real:** ~$68.00/mes

La diferencia principal es el nodo más grande (`B4pls_v2` vs `B2ps_v2`), compensado parcialmente por tener PostgreSQL in-cluster en vez de Azure Flexible Server.

---

## 4. Medidas de Ahorro Implementadas

| Medida | Ahorro Estimado |
|---|---|
| **AKS Tier Free** (vs Standard ~$73/mes) | **~$73.00/mes** |
| **Sin Load Balancers** (solo ClusterIP, ~$19/mes c/u) | **~$19.00/mes** |
| **PostgreSQL in-cluster** (vs Azure Flexible Server ~$25/mes) | **~$25.00/mes** |
| **Jaeger escalado a 0** (~0.1 vCPU, 128 MB) | **~$2.00/mes** |
| **Sin Premium Storage para PVCs** (Standard SSD vs Premium) | **~$3.00/mes** |
| **Sin backups automatizados de Azure** | **~$5.00/mes** |
| **Total ahorro estimado** | **~$127.00/mes** |

Sin estas medidas, el costo estimado sería de **~$195.00/mes**.

---

## 5. Oportunidades de Optimización Adicional

| Optimización | Ahorro Estimado | Riesgo / Consideración |
|---|---|---|
| **Reducir a B2ps_v2** (2 vCPU, 4 GB) | **~$30/mes** | Puede no ser suficiente para todos los servicios. Probar con monitoreo de recursos. |
| **Spot VM instance** (~60% descuento sobre VM) | **~$37/mes** | El nodo puede ser desalojado en cualquier momento. Solo para dev. |
| **Terraform destroy** cuando no se usa | **~$68/mes** (completo) | Requiere recrear el clúster al retomar. Guía en `GUIA_TERRAFORM.md`. |
| **Eliminar Elasticsearch 5Gi si no es crítico** | **~$2/mes** | Se pierde capacidad de búsqueda de logs. Prometheus + Grafana pueden ser suficientes. |
| **Reducir réplicas de infra** (Kafka, ES, Neo4j) | **~$5/mes** | Algunos servicios podrían funcionar con menos requests. |
| **Total optimizable** | **hasta ~$74/mes** | |

**Costo mínimo posible (dev, con todas las optimizaciones): ~$0/mes** (si se destruye el clúster) o **~$31/mes** (B2ps_v2 + spot, con infra mínima).

> **Recomendación**: Implementar **terraform destroy** como práctica habitual cuando no se trabaja en el proyecto. Es el "interruptor de ahorro" más efectivo.

---

## 6. Proyección: 3 Ambientes (dev + stage + prod)

Si se desplegaran los 3 ambientes según lo planeado en Terraform:

| Ambiente | VM | Nodos | Costo Estimado |
|---|---|---|---|
| Dev | B2ps_v2 | 1 | ~$31.00/mes |
| Stage | B2ps_v2 | 1 | ~$31.00/mes |
| Prod | B2ps_v2 | 1 | ~$31.00/mes |
| **Subtotal VMs** | | **3 nodos** | **~$93.00/mes** |
| Load Balancers (1 por ambiente) | | 3 LB | ~$57.00/mes |
| Azure PostgreSQL Flexible Server | | 1 (compartido) | ~$25.00/mes |
| Storage Account + VNet | | | ~$5.00/mes |
| **Total 3 ambientes** | | | **~$180.00/mes** |

> **Nota**: Los 3 ambientes solo serían necesarios si el proyecto estuviera en producción con usuarios reales. Para un proyecto académico, un solo ambiente **dev** es suficiente y óptimo en costo.

---

## 7. Resumen

| Escenario | Costo/mes |
|---|---|
| Actual (dev, B4pls_v2) | ~$68.00 |
| Planeado (Terraform dev, B2ps_v2) | ~$59.50 |
| Óptimo (dev, B2ps_v2, spot) | ~$31.00 |
| Mínimo (terraform destroy) | **$0.00** |
| 3 ambientes completos | ~$180.00 |
