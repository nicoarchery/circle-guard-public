provider "azurerm" {
  features {}
}

provider "kubernetes" {
  host                   = module.aks.kube_config.0.host
  client_certificate     = base64decode(module.aks.kube_config.0.client_certificate)
  client_key             = base64decode(module.aks.kube_config.0.client_key)
  cluster_ca_certificate = base64decode(module.aks.kube_config.0.cluster_ca_certificate)
}

module "network" {
  source              = "../../modules/network"
  resource_group_name = "${var.prefix}-rg"
  location            = var.location
  prefix              = var.prefix
}

module "aks" {
  source              = "../../modules/aks"
  resource_group_name = module.network.resource_group_name
  location            = module.network.location
  prefix              = var.prefix
  vnet_subnet_id      = module.network.subnet_id
  environment         = "dev"
}

module "database" {
  source              = "../../modules/database"
  resource_group_name = module.network.resource_group_name
  location            = module.network.location
  prefix              = var.prefix
  admin_password      = var.db_password
}
