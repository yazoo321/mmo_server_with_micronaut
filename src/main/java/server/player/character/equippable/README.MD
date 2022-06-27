# Equip items readme

#
blogpost covering introduction of this module:
TBD

Youtube video containing a summary and live testing:
TBD

###
The key parts of items is as follows:
- Items (see `Item.java`)
- Types (see `ItemType.java`)
- Slots (see `EquippedItems.java` and classes that extend it, like `HelmSlot.java`)

Each item should have a type, for example a weapon, helm, chest piece etc.
For each type, there should be a corresponding slot. For example a weapon should fit into a Weapon slot.
The design here is to create slots for each item type and have them reflect each other using `category`

###
Category is defined by enum `ItemType` and is consistent between `Item` and `EquippedItems`

### 
When you try to equip an item you will do the following things:

- Check that the user matches the header (which will be authenticated)
- Evaluate the category of the item you're trying to equip
- Locate an item if already equipped in that slot
- if already equipped, un-equip (by deleting the equpped slot entry) it and place it into inventory (fail if inventory full)
- equip new item by creating new instance of `EquippedItems`
