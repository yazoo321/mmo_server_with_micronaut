# Appearance README

## Concept
In this example we will use modular characters in unreal engine.
In order to modify characters appearance, you need to specify what part of the body you're modifying,
the skeletal mesh that you're assigning as well as the material instance to apply.
Therefore, we add columns for mesh and material in `AppearancePiece`. 
Likewise, equipment will also relate to appearance, therefore we add ID to the association.

## Dynamically evaluating options (hair, skin color, etc)
One way is to have hair options or skin color be evaluated statically from utils file or similar.
Alternatively, we're finding the options by looking up the part and returning the options from list as a set.



### Example JSON for creating AppearancePiece items
```aidl
{
    "appearancePieceList": [
        {
            "part": "HEAD",
            "mesh": "SK_Hu_M_Hair_01",
            "material": "MI_Hair_01_Bd",
            "race": "HUMAN",
            "isMale": true,
            "itemGroup": "",
            "itemID": "",
            "isBase": true,
            "isDefault": true,
        },
        {
            "part": "HEAD",
            "mesh": "SK_Hu_M_Hair_02",
            "material": "MI_Hair_01_Bd",
            "race": "HUMAN",
            "isMale": true,
            "itemGroup": "",
            "itemID": "",
            "isBase": true,
            "isDefault": true,
        }   
    ]
}
```

Note that itemGroup and itemId can be left out to provide null instead.
Each combination needs to have explicit entry. This is because of `many to many` 
relationship that items can have to materials.