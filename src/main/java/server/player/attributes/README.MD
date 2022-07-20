# What are attributes

Attributes are denormalized form of data attached to characters.
For example, stats are attributes. E.g. stamina, intelligence, strength etc.
Rather than having attributes as separate columns, we will use our `Tag` system to store them.
This allows our system to stay fully dynamic.
I.e. we can add and remove different attributes in future as we like, without breaking db.
