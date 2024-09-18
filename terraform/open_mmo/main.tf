provider "azurerm" {
  features {}
  version = "~> 4.2.0"
  subscription_id = var.azure_subscription_id
  client_id       = var.azure_client_id
  client_secret   = var.azure_client_secret
  tenant_id       = var.azure_tenant_id
}

provider "kubernetes" {
  host                   = azurerm_kubernetes_cluster.main.kube_config[0].host
  username               = azurerm_kubernetes_cluster.main.kube_config[0].username
  password               = azurerm_kubernetes_cluster.main.kube_config[0].password
  client_certificate     = base64decode(azurerm_kubernetes_cluster.main.kube_config[0].client_certificate)
  client_key             = base64decode(azurerm_kubernetes_cluster.main.kube_config[0].client_key)
  cluster_ca_certificate = base64decode(azurerm_kubernetes_cluster.main.kube_config[0].cluster_ca_certificate)
}

variable "azure_subscription_id" {
  description = "Subscription for azure"
  type        = string
}
variable "azure_client_id" {
  description = "Client/App ID"
  type        = string
}
variable "azure_client_secret" {
  description = "Client Secret / Password"
  type        = string
}
variable "azure_tenant_id" {
  description = "Tenant ID"
  type        = string
}
