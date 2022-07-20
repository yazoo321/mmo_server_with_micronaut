#Levels readme

## Levels as sub-attributes
Levels can be considered part of players attributes.
For in stance, we can have multiple classes, e.g. Mage.
Mage can have `Level 1`. Potentially we want to have items that modify
the class level. There may be spells which require a specific 'current level' of mage
to cast, so we can have these modifiers be part of attributes.

They could be refactored out later into its own module.

## How the level/class system is defined here.
The level system is defined to allow multi classing here.
Note that validations have not yet been put in place to set any limits.
The idea will be to allow user to multi-class for example to 3 classes.
We will store their general level, as well as the class levels in the tags.
`LevelAttributeTypes` keeps the general base data - i.e. the base level and the XP the user has.
`ClassesAttributeTypes` keeps the allowed classes the character can select (prior to validation).
