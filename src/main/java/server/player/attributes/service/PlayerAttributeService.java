package server.player.attributes.service;

import static server.attribute.stats.types.AttributeTypes.*;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Tag;
import server.items.equippable.service.EquipItemService;
import server.player.attributes.model.PlayerAttributes;
import server.player.attributes.repository.PlayerAttributesRepository;

@Singleton
@Slf4j
public class PlayerAttributeService {

    @Inject PlayerAttributesRepository attributesRepository;

    @Inject
    EquipItemService equipItemService;

    public PlayerAttributes createBaseAttributes(String playerName) {
        // at point of creating new character, reference all the base attributes that we use
        Map<String, Integer> baseAttributes =
                new HashMap<>(
                        Map.of(
                                STR.type, 10,
                                DEX.type, 10,
                                STA.type, 10,
                                INT.type, 10));

        Map<String, Integer> current =
                new HashMap<>(
                        Map.of(
                                CURRENT_HP.type, 100,
                                CURRENT_MP.type, 100,
                                PHY_AMP.type, 0,
                                MAG_AMP.type, 0,
                                DEF.type, 10,
                                MAG_DEF.type, 10,
                                ATTACK_SPEED.type, 50,
                                CAST_SPEED.type, 50,
                                PHY_CRIT.type, 5,
                                MGC_CRIT.type, 5
                        ));
        current.putAll(Map.of(
                MAX_HP.type, 100,
                MAX_MP.type, 100
        ));

        current.putAll(baseAttributes);

        PlayerAttributes playerAttributes =
                PlayerAttributes.builder()
                        .playerName(playerName)
                        .baseAttributes(baseAttributes)
                        .attributePoints(0)
                        .currentAttributes(current)
                        .build();

        attributesRepository.insertPlayerAttributes(playerAttributes);

        return playerAttributes;
    }

    public void addPlayerAttribute(String playerName, String attribute) {
        PlayerAttributes attributes = attributesRepository.findPlayerAttributes(playerName);
        Integer points = attributes.getAttributePoints() == null ? 0 : attributes.getAttributePoints();
        if (points < 1) {
            throw new RuntimeException("No points left to add");
        }

        Map<String, Integer> base = attributes.getBaseAttributes();
        Map<String, Integer> current = attributes.getCurrentAttributes();

        // adding a point will increment base, keep a counter at 'added' and affect the current
        // stats
        base.put(attribute, base.get(attribute) + 1);
        points--;
        attributes.setAttributePoints(points);
        attributesRepository.updatePlayerAttributes(playerName, attributes);
//        evaluateCurrentAttributes(playerName);
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

    public Map<String, Integer> getPlayerCurrentAttributes(String playerName) {
        return attributesRepository.findPlayerAttributes(playerName).getCurrentAttributes();

    }

//    public void evaluateCurrentAttributes(String playerName) {
//        PlayerAttributes attributes = getPlayerAttributes(playerName);
//        Map<String, Integer> currentAttributes = attributes.getCurrentAttributes();
//
//
//        equipItemService.getEquippedItems(playerName)
//               .map(equippedItems -> {
//                   equippedItems.forEach(item -> {
//                       List<Tag> tags = item.getItemInstance().getItem().getTags();
//                       tags.forEach(tag -> {
//                           try {
//                               Integer val = Integer.parseInt(tag.getValue());
//                               val += currentAttributes.getOrDefault(tag.getName(), 0);
//                               currentAttributes.put(tag.getName(), val);
//                           } catch (NumberFormatException e) {
//                               // we don't support anything other than int for now.
//                               log.warn("Evaluate current attributes threw exception, invalid types included");
//                           }
//                       });
//                   });
//
//                   attributesRepository.updateCurrentAttributes(playerName, currentAttributes)
//                           .doOnSuccess(attr -> {
//
//                           })
//                           .subscribe();
//               })
//               .subscribe();
//    }

    // TODO: Function to evaluate current attributes based on items, statuses, etc.

    public void removePlayerAttributes(String playerName) {
        attributesRepository.deletePlayerAttributes(playerName);
    }
}
