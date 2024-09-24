resource "azurerm_linux_virtual_machine" "micronaut_vm" {
  name                = "micronaut-vm"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  size                = "Standard_B2s"

  admin_username      = "azureuser"
  admin_password      = "Password1234!"

  disable_password_authentication = false

  network_interface_ids = [
    azurerm_network_interface.nic.id,
  ]

  os_disk {
    caching              = "ReadWrite"
    storage_account_type = "Standard_LRS"
  }

  source_image_reference {
    publisher = "Canonical"
    offer     = "UbuntuServer"
    sku       = "18.04-LTS"
    version   = "latest"
  }
}

resource "azurerm_public_ip" "micronaut_pip" {
  name                = "micronaut-pip"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  allocation_method   = "Static"
  sku                 = "Basic"
  lifecycle           {
    create_before_destroy = true
  }
  ip_version = "IPv4"  # You can duplicate this block for IPv6 or use "IPv4" and "IPv6" if available
}
# We will want to enable IPv6 in near future, but not utilised right now
#resource "azurerm_public_ip" "micronaut_ipv6_pip" {
#  name                = "micronaut-ipv6-pip"
#  location            = azurerm_resource_group.main.location
#  resource_group_name = azurerm_resource_group.main.name
#  allocation_method   = "Static"
#  sku                 = "Basic"
#  ip_version          = "IPv6"  # Enable IPv6
#  lifecycle           {
#    create_before_destroy = true
#  }
#}

## Define the Network Security Group
#resource "azurerm_network_security_group" "default_security_group" {
#  name                = "acceptanceTestSecurityGroup1"
#  location            = azurerm_resource_group.main.location
#  resource_group_name = azurerm_resource_group.main.name
#}
#
## Define outbound TCP rule
#resource "azurerm_network_security_rule" "outbound_tcp" {
#  name                        = "outbound_tcp"
#  priority                    = 100
#  direction                   = "Outbound"
#  access                      = "Allow"
#  protocol                    = "Tcp"
#  source_port_range           = "*"
#  destination_port_range      = "*"
#  source_address_prefix       = "*"
#  destination_address_prefix  = "*"
#  resource_group_name         = azurerm_network_security_group.default_security_group.resource_group_name
#  network_security_group_name = azurerm_network_security_group.default_security_group.name
#}
#
## Define inbound TCP rule
#resource "azurerm_network_security_rule" "inbound_tcp" {
#  name                        = "inbound_tcp"
#  priority                    = 101
#  direction                   = "Inbound"
#  access                      = "Allow"
#  protocol                    = "Tcp"
#  source_port_range           = "*"
#  destination_port_range      = "*"
#  source_address_prefix       = "*"
#  destination_address_prefix  = "*"
#  resource_group_name         = azurerm_network_security_group.default_security_group.resource_group_name
#  network_security_group_name = azurerm_network_security_group.default_security_group.name
#}
#
## Define inbound UDP rule
#resource "azurerm_network_security_rule" "inbound_udp" {
#  name                        = "inbound_udp"
#  priority                    = 102
#  direction                   = "Inbound"
#  access                      = "Allow"
#  protocol                    = "Udp"
#  source_port_range           = "*"
#  destination_port_range      = "*"
#  source_address_prefix       = "*"
#  destination_address_prefix  = "*"
#  resource_group_name         = azurerm_network_security_group.default_security_group.resource_group_name
#  network_security_group_name = azurerm_network_security_group.default_security_group.name
#}
#
## Define outbound UDP rule
#resource "azurerm_network_security_rule" "outbound_udp" {
#  name                        = "outbound_udp"
#  priority                    = 103
#  direction                   = "Outbound"
#  access                      = "Allow"
#  protocol                    = "Udp"
#  source_port_range           = "*"
#  destination_port_range      = "*"
#  source_address_prefix       = "*"
#  destination_address_prefix  = "*"
#  resource_group_name         = azurerm_network_security_group.default_security_group.resource_group_name
#  network_security_group_name = azurerm_network_security_group.default_security_group.name
#}

resource "kubernetes_service" "micronaut_service" {
  metadata {
    name      = "micronaut-service"
    namespace = kubernetes_namespace.main.metadata[0].name
  }

  spec {

    selector = {
      app = kubernetes_deployment.micronaut_vm.metadata[0].labels["app"]
    }

    type                    = "LoadBalancer"
    external_traffic_policy = "Local"
    ip_families             = ["IPv4"]
    ip_family_policy        = "SingleStack"

#   Disabled as incur costs and are not currently used
    #    ip_families             = ["IPv4", "IPv6"]  # Enable if using dual-stack
    #    ip_family_policy        = "RequireDualStack"  # Use "RequireDualStack" for dual-stack


    port {
      name        = "main"
      port        = 80      # External port to expose the service
      target_port = 8081    # Internal port of the Micronaut app
    }

    port {
      name        = "udp-9876"
      port        = 9876
      target_port = 9876
      protocol    = "UDP"
    }

    # Add the range for receiving updates over UDP ports 5000-5010
    port {
      name        = "udp-5000"
      port        = 5000
      target_port = 5000
      protocol    = "UDP"
    }

    port {
      name        = "udp-5001"
      port        = 5001
      target_port = 5001
      protocol    = "UDP"
    }

    port {
      name        = "udp-5002"
      port        = 5002
      target_port = 5002
      protocol    = "UDP"
    }

    # Continue adding for ports 5003-5010
    port {
      name        = "udp-5003"
      port        = 5003
      target_port = 5003
      protocol    = "UDP"
    }

    port {
      name        = "udp-5004"
      port        = 5004
      target_port = 5004
      protocol    = "UDP"
    }

    port {
      name        = "udp-5005"
      port        = 5005
      target_port = 5005
      protocol    = "UDP"
    }

    port {
      name        = "udp-5006"
      port        = 5006
      target_port = 5006
      protocol    = "UDP"
    }

    port {
      name        = "udp-5007"
      port        = 5007
      target_port = 5007
      protocol    = "UDP"
    }

    port {
      name        = "udp-5008"
      port        = 5008
      target_port = 5008
      protocol    = "UDP"
    }

    port {
      name        = "udp-5009"
      port        = 5009
      target_port = 5009
      protocol    = "UDP"
    }

    port {
      name        = "udp-5010"
      port        = 5010
      target_port = 5010
      protocol    = "UDP"
    }
  }
}
