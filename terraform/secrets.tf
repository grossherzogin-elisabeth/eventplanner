data "azurerm_key_vault" "eventplanner" {
  name                = "kv-lissi"
  resource_group_name = "rg-lissi"
}

data "azurerm_key_vault_secret" "client_id" {
  name         = "cognito-client-id"
  key_vault_id = data.azurerm_key_vault.eventplanner.id
}

data "azurerm_key_vault_secret" "client_secret" {
  name         = "cognito-client-secret"
  key_vault_id = data.azurerm_key_vault.eventplanner.id
}

data "azurerm_key_vault_certificate" "ssl_certificate" {
  name         = "cert-crew-grossherzogin-elisabeth-de"
  key_vault_id = data.azurerm_key_vault.eventplanner.id
}
