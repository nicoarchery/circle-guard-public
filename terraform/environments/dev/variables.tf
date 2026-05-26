variable "prefix" {
  description = "Prefix for all resources"
  type        = string
  default     = "circleguard"
}

variable "location" {
  description = "Azure region"
  type        = string
  default     = "southcentralus"
}

variable "vm_size" {
  description = "Size of the virtual machine"
  type        = string
  default     = "standard_b2ps_v2"
}

variable "db_password" {
  description = "Password for the PostgreSQL database"
  type        = string
  sensitive   = true
}
