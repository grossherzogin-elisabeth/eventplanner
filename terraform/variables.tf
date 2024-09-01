variable "region" {
  description = "Azure infrastructure region"
  type        = string
  default     = "germanywestcentral"
}

variable "env" {
  description = "Application environment (live, dev, ...)"
  type        = string
  default     = "live"
}

variable "domain" {
  description = "Domain of the application"
  type        = string
  default     = "crew.grossherzogin-elisabeth.de"
}

