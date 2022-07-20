package server.player.attributes.levels.service;

import server.common.dto.NumTag;
import server.player.attributes.levels.types.ClassesAttributeTypes;
import server.player.attributes.levels.types.LevelAttributeTypes;
import server.player.attributes.model.PlayerAttributes;
import server.player.attributes.repository.PlayerAttributesRepository;
import server.player.attributes.service.PlayerAttributeService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class PlayerLevelAttributeService {

    @Inject
    PlayerAttributeService playerAttributeService;
    
    @Inject
    PlayerAttributesRepository attributesRepository;


    public static final List<String> AVAILABLE_CLASSES = List.of(
            ClassesAttributeTypes.MAGE.getType(),
            ClassesAttributeTypes.FIGHTER.getType(),
            ClassesAttributeTypes.CLERIC.getType()
    );

    public PlayerAttributes initializeCharacterClass(String playerName, String playerClass) {
        if (!isClassValid(playerClass)) {
            throw new RuntimeException("Invalid class selected");
        }

        PlayerAttributes attributes = playerAttributeService.getPlayerAttributes(playerName);
        List<NumTag> baseAttr = attributes.getBaseAttributes();
        List<NumTag> currentAttr = attributes.getCurrentAttributes();

        List<NumTag> toAdd = new ArrayList<>(List.of(
                // base level attr
                new NumTag(LevelAttributeTypes.LEVEL.type, 1),
                new NumTag(LevelAttributeTypes.XP.type, 0)
        ));

        // next add the levels for other classes
        // This will set each class level to 0, except for the class the user chose at character selection.
        // This in theory allows us to multi-class, by keeping track of individual levels in attributes.
        AVAILABLE_CLASSES.forEach(c-> {
            Integer level = c.equalsIgnoreCase(playerClass) ? 1 : 0;
            toAdd.add(new NumTag(c, level));
        });

        // store to base and current
        baseAttr.addAll(toAdd);
        currentAttr.addAll(toAdd);

        attributes.setBaseAttributes(baseAttr);
        attributes.setCurrentAttributes(currentAttr);

        attributesRepository.updatePlayerAttributes(playerName, attributes);

        return attributes;
    }

    public void handleLevelUp(String playerName, String classToLevel) {
        // TODO: Handle validation, does user have enough xp for level up? Are they able to level in this class?
        PlayerAttributes attributes = playerAttributeService.getPlayerAttributes(playerName);
        List<NumTag> baseAttr = attributes.getBaseAttributes();
        List<NumTag> currentAttr = attributes.getCurrentAttributes();

        NumTag classLevelBase = PlayerAttributeService.findTag(baseAttr, classToLevel);
        // TODO: This can be evaluated using a hook, which calls to evaluate all 'current' attributes.
        NumTag classLevelCurrent = PlayerAttributeService.findTag(currentAttr, classToLevel);

        classLevelBase.setValue(classLevelBase.getValue() + 1);
        classLevelCurrent.setValue(classLevelCurrent.getValue() + 1);

        attributesRepository.updatePlayerAttributes(playerName, attributes);
    }

    public void addPlayerXp(String playerName, Integer xpToAdd) {
        // we may keep XP in base + current (tech debt), but only need to add in one place

        PlayerAttributes attributes = playerAttributeService.getPlayerAttributes(playerName);
        List<NumTag> baseAttr = attributes.getBaseAttributes();

        NumTag currentXp = PlayerAttributeService.findTag(baseAttr, LevelAttributeTypes.XP.type);
        currentXp.setValue(currentXp.getValue() + xpToAdd);

        attributesRepository.updatePlayerAttributes(playerName, attributes);
    }

    private boolean isClassValid(String className) {
        // consider other validations

        return AVAILABLE_CLASSES.contains(className);
    }
}
