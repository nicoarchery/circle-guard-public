provider "azurerm" {
  features {}
}

terraform {
  backend "azurerm" {
    resource_group_name  = "rg-terraform-state"
    storage_account_name = "stcircleguardtfstate"
    container_name       = "tfstate"
    key                  = "prod.terraform.tfstate"
  }
}

provider "kubernetes" {
  host                   = module.aks.kube_config.0.host
  client_certificate     = base64decode(module.aks.kube_config.0.client_certificate)
  client_key             = base64decode(module.aks.kube_config.0.client_key)
  cluster_ca_certificate = base64decode(module.aks.kube_config.0.cluster_ca_certificate)
}

module "network" {
  source              = "../../modules/network"
  resource_group_name = "${var.prefix}-prod-rg"
  location            = var.location
  prefix              = "${var.prefix}-prod"
}

module "aks" {
  source              = "../../modules/aks"
  resource_group_name = module.network.resource_group_name
  location            = module.network.location
  prefix              = "${var.prefix}-prod"
  vnet_subnet_id      = module.network.subnet_id
  environment         = "prod"
}

module "database" {
  source              = "../../modules/database"
  resource_group_name = module.network.resource_group_name
  location            = module.network.location
  prefix              = "${var.prefix}-prod"
  admin_password      = var.db_password
}

variable "prefix" {
  default = "circleguard"
}

variable "location" {
  default = "southcentralus"
}

variable "db_password" {
  type      = string
  sensitive = true
  default   = "ChangeMe123!"
}
