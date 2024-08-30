variable "region" {
  description = "Azure infrastructure region"
  type        = string
  default     = "germanywestcentral"
}

variable "app" {
  description = "Application that we want to deploy"
  type        = string
  default     = "eventplanner"
}

variable "env" {
  description = "Application env"
  type        = string
  default     = "live"
}

variable "domain" {
  description = "Domain of the application"
  type        = string
  default     = "crew.grossherzogin-elisabeth.de"
}

