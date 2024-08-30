terraform {
  required_version = ">= 1.3"
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "=3.52.0"
    }
  }
}

provider "azurerm" {
  features {}
  client_id = "5dbeb429-820d-4f7c-bb17-dc98eac149df"
  #client_secret   = "**********************"
  tenant_id       = "63384ddf-6496-44bd-b22c-93e944e6ed88"
  subscription_id = "7cb8180c-c0ab-43e3-9088-7b1b359cc861"
}

# https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs/guides/service_principal_client_secret
# Expires 01/01/2025
#export ARM_CLIENT_ID="5dbeb429-820d-4f7c-bb17-dc98eac149df"
#export ARM_CLIENT_SECRET=""
#export ARM_SUBSCRIPTION_ID="7cb8180c-c0ab-43e3-9088-7b1b359cc861"
#export ARM_TENANT_ID="63384ddf-6496-44bd-b22c-93e944e6ed88"
