package server.player.attributes.levels.service;

import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import server.common.attributes.types.ClassesAttributeTypes;
import server.common.attributes.types.LevelAttributeTypes;
import server.player.attributes.model.PlayerAttributes;
import server.player.attributes.repository.PlayerAttributesRepository;
import server.player.attributes.service.PlayerAttributeService;
import server.player.exceptions.CharacterException;

public class PlayerLevelAttributeService {

    @Inject PlayerAttributeService playerAttributeService;

    @Inject PlayerAttributesRepository attributesRepository;

    public static final List<String> AVAILABLE_CLASSES =
            List.of(
                    ClassesAttributeTypes.MAGE.getType(),
                    ClassesAttributeTypes.FIGHTER.getType(),
                    ClassesAttributeTypes.CLERIC.getType());

    public PlayerAttributes initializeCharacterClass(String playerName, String playerClass) {
        if (!isClassValid(playerClass)) {
            throw new CharacterException("Invalid class selected");
        }

        PlayerAttributes attributes = playerAttributeService.getPlayerAttributes(playerName);
        Map<String, Integer> baseAttr = attributes.getBaseAttributes();
        Map<String, Integer> currentAttr = attributes.getCurrentAttributes();

        Map<String, Integer> toAdd =
                new HashMap<>(
                        Map.of(
                                // base level attr
                                LevelAttributeTypes.LEVEL.type, 1,
                                LevelAttributeTypes.XP.type, 0));
        baseAttr.putAll(toAdd);
        currentAttr.putAll(toAdd);

        // next add the levels for other classes
        // This will set each class level to 0, except for the class the user chose at character
        // selection.
        // This in theory allows us to multi-class, by keeping track of individual levels in
        // attributes.
        AVAILABLE_CLASSES.forEach(
                c -> {
                    Integer level = c.equalsIgnoreCase(playerClass) ? 1 : 0;
                    baseAttr.put(c, level);
                    currentAttr.put(c, level);
                });

        //        attributes.setBaseAttributes(baseAttr);
        //        attributes.setCurrentAttributes(currentAttr);

        attributesRepository.updatePlayerAttributes(playerName, attributes);

        return attributes;
    }

    public void handleLevelUp(String playerName, String classToLevel) {
        // TODO: Handle validation, does user have enough xp for level up? Are they able to level in
        // this class?
        PlayerAttributes attributes = playerAttributeService.getPlayerAttributes(playerName);
        Map<String, Integer> baseAttr = attributes.getBaseAttributes();
        Map<String, Integer> currentAttr = attributes.getCurrentAttributes();

        baseAttr.put(classToLevel, baseAttr.get(classToLevel) + 1);
        currentAttr.put(classToLevel, currentAttr.get(classToLevel) + 1);

        attributesRepository.updatePlayerAttributes(playerName, attributes);
    }

    public void addPlayerXp(String playerName, Integer xpToAdd) {
        // we may keep XP in base + current (tech debt), but only need to add in one place

        PlayerAttributes attributes = playerAttributeService.getPlayerAttributes(playerName);
        Map<String, Integer> baseAttr = attributes.getBaseAttributes();

        baseAttr.put(
                LevelAttributeTypes.XP.type, baseAttr.get(LevelAttributeTypes.XP.type) + xpToAdd);

        attributesRepository.updatePlayerAttributes(playerName, attributes);
    }

    private boolean isClassValid(String className) {
        // consider other validations

        return AVAILABLE_CLASSES.contains(className);
    }
}
