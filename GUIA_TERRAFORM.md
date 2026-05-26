# 🧱 Guía paso a paso: Infraestructura con Terraform en AZURE

Esta guía detalla las acciones para cumplir con el Requisito 2 usando **Azure**.

---

## 🟢 1. La variable de sesión (¡Muy importante!)
Recuerda que cada vez que cierras tu terminal y la vuelves a abrir, las credenciales (las variables `ARM_...`) se borran de la memoria.

**¿Cómo saber si están ahí?** Escribe en tu terminal:
```bash
echo $ARM_CLIENT_ID
```
Si no te sale nada, debes volver a ejecutar estos comandos reemplazando con tus datos:
```bash
export ARM_CLIENT_ID="tu-appId"
export ARM_CLIENT_SECRET="tu-password"
export ARM_TENANT_ID="tu-tenant"
export ARM_SUBSCRIPTION_ID="tu-subscriptionId"
```

---

## 🟢 2. Estructura de carpetas
Asegúrate siempre de estar posicionado en la carpeta correcta antes de ejecutar los comandos:
```bash
cd ~/Documents/SeptimoSemestre/Ingesoft/Proyecto\ final/circle-guard-public/terraform/environments/dev
```
*(Si estás en esa carpeta, Terraform encontrará automáticamente tu `backend.tf` y los módulos que configuramos).*

---

## 🟢 3. El ciclo de vida (Tu "interruptor" de ahorro)
Como ya está todo configurado, para tu rutina diaria de trabajo, este es el flujo que usarás:

### **Para encender (Desplegar infraestructura):**
```bash
terraform init      # Solo lo haces la primera vez de la sesión o si cambias algo de módulos
terraform apply -auto-approve
```

### **Para apagar (Ahorro total de créditos):**
```bash
terraform destroy -auto-approve
```

---

## 🛑 4. Frenado de Emergencia (Si el destroy normal falla)
Si al intentar borrar te sale un error de "Resource Not Found" (404) porque ya borraste algo manualmente, o si simplemente quieres forzar la limpieza del estado local, usa:

```bash
terraform destroy -refresh=false -auto-approve
```
Este comando limpiará tu memoria de Terraform sin intentar validar cada recurso en la nube, asegurando que el estado quede en cero y no se sigan consumiendo créditos accidentalmente.

---

## ✅ Checklist de Entrega (Puntos 1, 2 y 3)
- [x] Rama: `main` (Branching strategy documentada)
- [x] Terraform: Multi-ambiente con backend remoto.
- [x] Patrones: Circuit Breaker implementado en código.
