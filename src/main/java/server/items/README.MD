# Items readme

## Documentation and videos

Introduction of inventory using MongoDB:
post: https://unreal-mmo-dev.com/2022/05/11/creating-mmo-rpg-style-inventory-system-with-java-and-mongodb/
Youtube video containing a summary and live testing:
https://youtu.be/Udm_cTfi7ms

Inventory overview (Aug 2024) and drag and drop functionality with UE:
https://unreal-mmo-dev.com/2024/09/02/devlog-64-how-to-add-drag-and-drop-to-inventory-in-ue/

UE Specific, how to create animated character in inventory widget: https://unreal-mmo-dev.com/2023/01/23/33-unreal-engine-inventory-character/

Integrating dynamic inventory with UE (deprecated parts): https://unreal-mmo-dev.com/2023/06/29/44-how-to-integrate-dynamic-mmo-inventory-with-your-unreal-engine/

Handle RPG Style stats and equipping items: https://unreal-mmo-dev.com/2023/07/19/45-how-to-handle-rpg-stats-system-and-equipping-items-in-java/

Integrating equipping items and stats system: https://unreal-mmo-dev.com/2023/07/21/46-how-to-integrate-equipping-items-and-stats-system-in-ue-using-websockets/

Modify character mesh on item equips: https://unreal-mmo-dev.com/2023/09/03/48-how-to-modify-your-character-mesh-on-item-equips-in-unreal-engine/

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

## 
To create some items easily via API, some post data is available in `src/main/resources/items/create-items.txt`
