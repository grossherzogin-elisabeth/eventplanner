output "azurerm_container_app_url" {
  value = azurerm_container_app.eventplanner.latest_revision_fqdn
}
