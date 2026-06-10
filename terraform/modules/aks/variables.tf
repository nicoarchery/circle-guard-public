variable "resource_group_name" {
  type = string
}

variable "location" {
  type = string
}

variable "prefix" {
  type = string
}

variable "node_count" {
  type    = number
  default = 1
}

variable "vm_size" {
  type    = string
  default = "standard_b2ps_v2"
}

variable "vnet_subnet_id" {
  type = string
}

variable "environment" {
  type = string
}
