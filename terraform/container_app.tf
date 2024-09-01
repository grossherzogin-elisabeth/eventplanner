resource "azurerm_container_app_environment" "eventplanner" {
  name                       = "cae-${local.stack}"
  location                   = azurerm_resource_group.eventplanner.location
  resource_group_name        = azurerm_resource_group.eventplanner.name
  log_analytics_workspace_id = azurerm_log_analytics_workspace.eventplanner.id
  tags                       = local.default_tags
}

resource "azurerm_container_app" "eventplanner" {
  name                         = "ca-${local.stack}"
  container_app_environment_id = azurerm_container_app_environment.eventplanner.id
  resource_group_name          = azurerm_resource_group.eventplanner.name
  revision_mode                = "Single"
  tags                         = local.default_tags

  ingress {
    allow_insecure_connections = true
    external_enabled           = true
    target_port                = 80

    traffic_weight {
      percentage      = 100
      latest_revision = true
    }

    # custom_domain {
    #   name                     = var.domain
    #   certificate_id           = data.azurerm_key_vault_certificate.ssl_certificate.id
    #   certificate_binding_type = "SniEnabled"
    # }
  }

  lifecycle {
    // Required to not delete the manually created custom domain since it is not possible to create a managed certificate for a custom domain with terraform
    // https://github.com/hashicorp/terraform-provider-azurerm/issues/21866
    ignore_changes = [
      #ingress
    ]
  }

  template {
    min_replicas = 1
    max_replicas = 1

    volume {
      name         = "vol-eventplanner"
      storage_name = azurerm_container_app_environment_storage.eventplanner.name
      storage_type = "AzureFile"
    }

    container {
      name   = "eventplanner"
      image  = "ghcr.io/grossherzogin-elisabeth/eventplanner:latest"
      cpu    = 0.5
      memory = "1Gi"

      volume_mounts {
        name = "vol-eventplanner"
        path = "/app/data"
      }

      env {
        name  = "OAUTH_ISSUER_URI"
        value = "https://cognito-idp.eu-central-1.amazonaws.com/eu-central-1_erXnjLJc7"
      }
      env {
        name        = "OAUTH_CLIENT_ID"
        secret_name = "client-id"
      }
      env {
        name        = "OAUTH_CLIENT_SECRET"
        secret_name = "client-secret"
      }
      env {
        name        = "USERS_EXCEL_PASSWORD"
        secret_name = "users-excel-password"
      }
      env {
        name        = "DATA_ENCRYPTION_PASSWORD"
        secret_name = "data-encryption-password"
      }
      env {
        name  = "SERVER_HOST"
        value = var.domain
      }
    }
  }

  secret {
    name  = "client-id"
    value = data.azurerm_key_vault_secret.client_id.value
  }

  secret {
    name  = "client-secret"
    value = data.azurerm_key_vault_secret.client_secret.value
  }

  secret {
    name  = "users-excel-password"
    value = data.azurerm_key_vault_secret.users_excel_password.value
  }

  secret {
    name  = "data-encryption-password"
    value = data.azurerm_key_vault_secret.data_encryption_password.value
  }
}
