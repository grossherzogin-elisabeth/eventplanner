locals {
  stack = "eventplanner-${var.env}"

  default_tags = {
    environment = var.env
    app         = "eventplanner"
  }
}