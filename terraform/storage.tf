resource "azurerm_storage_account" "eventplanner" {
  name                     = "steventplanner"
  resource_group_name      = azurerm_resource_group.eventplanner.name
  location                 = azurerm_resource_group.eventplanner.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
  tags                     = local.default_tags
}

resource "azurerm_storage_share" "eventplanner" {
  name                 = "share-eventplanner"
  storage_account_name = azurerm_storage_account.eventplanner.name
  quota                = 5
}

resource "azurerm_container_app_environment_storage" "eventplanner" {
  name                         = "caes-eventplanner"
  container_app_environment_id = azurerm_container_app_environment.eventplanner.id
  account_name                 = azurerm_storage_account.eventplanner.name
  share_name                   = azurerm_storage_share.eventplanner.name
  access_key                   = azurerm_storage_account.eventplanner.primary_access_key
  access_mode                  = "ReadWrite"
}
