locals {
  stack = "${var.app}-${var.env}"

  default_tags = {
    environment = var.env
    app         = var.app
  }
}

resource "azurerm_resource_group" "lissi_eventplanner" {
  name     = "rg-${local.stack}"
  location = var.region
  tags     = local.default_tags
}

resource "azurerm_log_analytics_workspace" "lissi_eventplanner" {
  name                = "log-${local.stack}"
  location            = azurerm_resource_group.lissi_eventplanner.location
  resource_group_name = azurerm_resource_group.lissi_eventplanner.name
  sku                 = "PerGB2018"
  retention_in_days   = 30
  tags                = local.default_tags
}
