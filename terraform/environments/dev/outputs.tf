output "aks_cluster_name" {
  value = module.aks.kubernetes_cluster_name
}

output "resource_group_name" {
  value = module.network.resource_group_name
}

output "postgresql_host" {
  value = module.database.postgresql_host
}
