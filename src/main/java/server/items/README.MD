# Items readme

#
blogpost covering introduction of this module:

https://ylazarev.com/2022/05/11/creating-mmo-rpg-style-inventory-system-with-java-and-mongodb/

Youtube video containing a summary and live testing:
https://youtu.be/Udm_cTfi7ms

###
The key parts of items is as follows:
- category
- tags key value pairs

The category defines what class the items belong to
###
Category is defined by enum `ItemType`
###
For example: Armour has category `ARMOUR`. 
When this is the case, you should use the `Armour.java` class
which extends your `Items` class.
The `Armour.java` class should be able to get any armour specific 
data via tags. For example it will know to look for `armour` tag
which is not relevant to `weapon` etc.

###
This way we can keep generic model for all items keeping db model simpler.
Furthermore we link the items to users inventory via `item id` and `player name`.

##
Items should be created via a migration. 
They can be spawned by a service which can randomly generate the item.
You can create a loot drop table or similar for mobs, or assign value range to items.
