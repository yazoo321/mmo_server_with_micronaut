# Items readme

###
The structure of items is as follows:
- category
- tags key value pairs
###
Category is defined by enum `ItemType`
###
For example: Armour has category `Armour`. 
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
