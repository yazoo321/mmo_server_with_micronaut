package server.player.attributes.service;

import lombok.extern.slf4j.Slf4j;
import server.common.dto.NumTag;
import server.player.attributes.model.PlayerAttributes;
import server.player.attributes.repository.PlayerAttributesRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import static server.player.attributes.types.AttributeTypes.*;

@Singleton
@Slf4j
public class PlayerAttributeService {

    @Inject
    PlayerAttributesRepository attributesRepository;


    public PlayerAttributes createBaseAttributes(String playerName) {
        // at point of creating new character, reference all the base attributes that we use
        List<NumTag> baseAttributes = new ArrayList<>(List.of(
                new NumTag(STR.type, 10),
                new NumTag(DEX.type, 10),
                new NumTag(STA.type, 10),
                new NumTag(INT.type, 10)
        ));

        List<NumTag> added = new ArrayList<>(List.of(
                new NumTag(STR.type, 0),
                new NumTag(DEX.type, 0),
                new NumTag(STA.type, 0),
                new NumTag(INT.type, 0)
        ));

        // some attributes are not base but evaluated runtime, e.g. attack speed

        List<NumTag> current = new ArrayList<>(List.of(
                new NumTag(HP.type, 100),
                new NumTag(MP.type, 100),

                new NumTag(PHY_AMP.type, 0),
                new NumTag(MAG_AMP.type, 0),

                new NumTag(DEF.type, 10),
                new NumTag(MAG_DEF.type, 10),

                new NumTag(ATTACK_SPEED.type, 50),
                new NumTag(CAST_SPEED.type, 50),

                new NumTag(PHY_CRIT.type, 5),
                new NumTag(MGC_CRIT.type, 5)
        ));

        current.addAll(baseAttributes);

        PlayerAttributes playerAttributes = PlayerAttributes.builder()
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

        List<NumTag> base = attributes.getBaseAttributes();
        List<NumTag> added = attributes.getAttributesAdded();
        List<NumTag> current = attributes.getCurrentAttributes();

        // adding a point will increment base, keep a counter at 'added' and affect the current stats
        NumTag tag = findTag(base, attribute);
        tag.setValue(tag.getValue()+1);

        tag = findTag(added, attribute);
        tag.setValue(tag.getValue()+1);

        tag = findTag(current, attribute);
        tag.setValue(tag.getValue()+1);

        attributes.setAttributesAdded(added);
        attributes.setCurrentAttributes(current);

        attributesRepository.updatePlayerAttributes(playerName, attributes);
    }

    public void modifyCurrentAttribute(String playerName, String attribute, Integer value) {
        PlayerAttributes attributes = attributesRepository.findPlayerAttributes(playerName);
        List<NumTag> current = attributes.getCurrentAttributes();

        NumTag tag = findTag(current, attribute);
        tag.setValue(tag.getValue() + value);

        attributes.setCurrentAttributes(current);

        attributesRepository.updatePlayerAttributes(playerName, attributes);
    }

    public PlayerAttributes getPlayerAttributes(String playerName) {
        return attributesRepository.findPlayerAttributes(playerName);
    }

    public void evaluateCurrentTags(String playerName) {
        PlayerAttributes attributes = attributesRepository.findPlayerAttributes(playerName);
        List<NumTag> baseAttr = attributes.getBaseAttributes();




    }

    // TODO: Function to evaluate current attributes based on items, statuses, etc.

    public static NumTag findTag(List<NumTag> tags, String attribute) {
        // can be moved to a utility class
        return tags.stream().filter(t ->
                        t.getName().equalsIgnoreCase(attribute))
                .findFirst()
                .orElseThrow();
    }

    public void removePlayerAttributes(String playerName) {
        attributesRepository.deletePlayerAttributes(playerName);
    }
}
