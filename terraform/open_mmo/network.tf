resource "azurerm_virtual_network" "vnet" {
  name                = "micronaut-vnet"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  address_space       = ["10.0.0.0/16"]
#  address_space       = ["10.0.0.0/16", "fd00::/48"]
}

resource "azurerm_subnet" "subnet" {
  name                 = "micronaut-subnet"
  resource_group_name  = azurerm_resource_group.main.name
  virtual_network_name = azurerm_virtual_network.vnet.name
  address_prefixes     = ["10.0.1.0/24"]
    #  address_prefixes     = ["10.0.1.0/24", "fd00::/64"]
}

resource "azurerm_network_interface" "nic" {
  name                = "nic-micronaut"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  ip_configuration {
    name                          = "internal"
    subnet_id                     = azurerm_subnet.subnet.id
    private_ip_address_allocation = "Dynamic"
    public_ip_address_id          = azurerm_public_ip.micronaut_pip.id
    primary                       = true
  }

#  ip_configuration {
#    name                          = "internal-ipv6"
#    subnet_id                     = azurerm_subnet.subnet.id
#    private_ip_address_allocation = "Dynamic"
#    private_ip_address_version    = "IPv6"
#    primary                       = false
#  }
}
