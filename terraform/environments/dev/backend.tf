terraform {
  backend "azurerm" {
    resource_group_name  = "rg-terraform-state"
    storage_account_name = "stcircleguardtfstate" # CAMBIAR si usaste otro nombre
    container_name       = "tfstate"
    key                  = "dev.terraform.tfstate"
  }
}
