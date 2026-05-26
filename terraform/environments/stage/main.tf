provider "azurerm" {
  features {}
}

terraform {
  backend "azurerm" {
    resource_group_name  = "rg-terraform-state"
    storage_account_name = "stcircleguardtfstate"
    container_name       = "tfstate"
    key                  = "stage.terraform.tfstate"
  }
}

module "network" {
  source              = "../../modules/network"
  resource_group_name = "${var.prefix}-stage-rg"
  location            = var.location
  prefix              = "${var.prefix}-stage"
}

module "aks" {
  source              = "../../modules/aks"
  resource_group_name = module.network.resource_group_name
  location            = module.network.location
  prefix              = "${var.prefix}-stage"
  vnet_subnet_id      = module.network.subnet_id
  environment         = "stage"
}

# Database commented out to save costs until needed
# module "database" {
#   source              = "../../modules/database"
#   resource_group_name = module.network.resource_group_name
#   location            = module.network.location
#   prefix              = "${var.prefix}-stage"
#   admin_password      = var.db_password
# }

variable "prefix" { default = "circleguard" }
variable "location" { default = "southcentralus" }
variable "db_password" { type = string, sensitive = true, default = "ChangeMe123!" }
