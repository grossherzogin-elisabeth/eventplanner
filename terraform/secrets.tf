data "azurerm_key_vault" "lissi_eventplanner" {
  name                = "kv-lissi"
  resource_group_name = "rg-lissi"
}

data "azurerm_key_vault_secret" "client_id" {
  name         = "auth-client-id"
  key_vault_id = data.azurerm_key_vault.lissi_eventplanner.id
}

data "azurerm_key_vault_secret" "client_secret" {
  name         = "auth-client-secret"
  key_vault_id = data.azurerm_key_vault.lissi_eventplanner.id
}

data "azurerm_key_vault_certificate" "ssl_certificate" {
  name         = "cert-crew-grosses-meer-surf"
  key_vault_id = data.azurerm_key_vault.lissi_eventplanner.id
}
