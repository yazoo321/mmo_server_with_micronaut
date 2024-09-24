resource "azurerm_kubernetes_cluster" "main" {
  name                = "myAKSCluster"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  dns_prefix          = "myakscluster"

  default_node_pool {
    name       = "default"
    node_count = 1
    vm_size    = "Standard_B2s"
  }

  identity {
    type = "SystemAssigned"
  }

#  role_based_access_control {
#    enabled = true
#  }

#  Requires Standard load balancer, allows us to use ipv6
#  network_profile {
#    network_plugin    = "kubenet"
#    load_balancer_sku = "standard"
##    ip_versions       = ["IPv4"]
#    ip_versions = ["IPv4", "IPv6"]
#
#    load_balancer_profile {
#      managed_outbound_ip_count = 1
#      managed_outbound_ipv6_count = 1
#    }
#  }

  tags = {
    environment = "production"
  }

#  depends_on = [azurerm_container_registry.acr]
}

# Define the Azure Container Registry
resource "azurerm_container_registry" "acr" {
  name                = "openmmoregistry"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  sku                 = "Basic"
#  admin_enabled       = false
}
#data "azurerm_user_assigned_identity" "agentpool_identity" {
#  name                = "${azurerm_kubernetes_cluster.main.name}-agentpool"
#  resource_group_name = azurerm_kubernetes_cluster.main.node_resource_group
#  depends_on          = [azurerm_kubernetes_cluster.main, azurerm_kubernetes_cluster_node_pool.worker_node_pool]
#}

resource "azurerm_role_assignment" "acrpull" {
  scope                = azurerm_container_registry.acr.id
  role_definition_name = "ACRPull"
  principal_id         = azurerm_kubernetes_cluster.main.kubelet_identity[0].object_id
}
# Attach the ACR to the AKS cluster
#resource "azurerm_role_assignment" "acr_to_aks" {
#  principal_id          = azurerm_kubernetes_cluster.main.kubelet_identity[0].object_id
#  role_definition_name  = "AcrPull"
#  scope                 = azurerm_container_registry.acr.id
#  skip_service_principal_aad_check = true
#
#  depends_on = [
#    azurerm_container_registry.acr,
#    azurerm_kubernetes_cluster.main
#  ]
##  depends_on            = [azurerm_kubernetes_cluster.main]
#}

output "client_certificate" {
  value     = azurerm_kubernetes_cluster.main.kube_config[0].client_certificate
  sensitive = true
}

output "kube_config" {
  value = azurerm_kubernetes_cluster.main.kube_config_raw
  sensitive = true
}
