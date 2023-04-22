package server.player.attributes.service;

import static server.player.attributes.types.AttributeTypes.*;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import server.player.attributes.model.PlayerAttributes;
import server.player.attributes.repository.PlayerAttributesRepository;

@Singleton
@Slf4j
public class PlayerAttributeService {

    @Inject PlayerAttributesRepository attributesRepository;

    public PlayerAttributes createBaseAttributes(String playerName) {
        // at point of creating new character, reference all the base attributes that we use
        Map<String, Integer> baseAttributes =
                new HashMap<>(
                        Map.of(
                                STR.type, 10,
                                DEX.type, 10,
                                STA.type, 10,
                                INT.type, 10));

        Map<String, Integer> added =
                new HashMap<>(
                        Map.of(
                                STR.type, 0,
                                DEX.type, 0,
                                STA.type, 0,
                                INT.type, 0));

        // some attributes are not base but evaluated runtime, e.g. attack speed

        Map<String, Integer> current =
                new HashMap<>(
                        Map.of(
                                HP.type, 100,
                                MP.type, 100,
                                PHY_AMP.type, 0,
                                MAG_AMP.type, 0,
                                DEF.type, 10,
                                MAG_DEF.type, 10,
                                ATTACK_SPEED.type, 50,
                                CAST_SPEED.type, 50,
                                PHY_CRIT.type, 5,
                                MGC_CRIT.type, 5));

        current.putAll(baseAttributes);

        PlayerAttributes playerAttributes =
                PlayerAttributes.builder()
                        .playerName(playerName)
                        .baseAttributes(baseAttributes)
                        .attributesAdded(added)
                        .currentAttributes(current)
                        .build();

        attributesRepository.insertPlayerAttributes(playerAttributes);

        return playerAttributes;
    }

    public void addPlayerAttribute(String playerName, String attribute) {
        PlayerAttributes attributes = attributesRepository.findPlayerAttributes(playerName);

        Map<String, Integer> base = attributes.getBaseAttributes();
        Map<String, Integer> added = attributes.getAttributesAdded();
        Map<String, Integer> current = attributes.getCurrentAttributes();

        // adding a point will increment base, keep a counter at 'added' and affect the current
        // stats
        base.put(attribute, base.get(attribute) + 1);
        added.put(attribute, added.get(attribute) + 1);
        current.put(attribute, current.get(attribute) + 1);

        attributesRepository.updatePlayerAttributes(playerName, attributes);
    }

    public void modifyCurrentAttribute(String playerName, String attribute, Integer value) {
        PlayerAttributes attributes = attributesRepository.findPlayerAttributes(playerName);
        Map<String, Integer> current = attributes.getCurrentAttributes();

        current.put(attribute, current.get(attribute) + value);

        attributes.setCurrentAttributes(current);

        attributesRepository.updatePlayerAttributes(playerName, attributes);
    }

    public PlayerAttributes getPlayerAttributes(String playerName) {
        return attributesRepository.findPlayerAttributes(playerName);
    }

    // TODO: Function to evaluate current attributes based on items, statuses, etc.

    public void removePlayerAttributes(String playerName) {
        attributesRepository.deletePlayerAttributes(playerName);
    }
}
