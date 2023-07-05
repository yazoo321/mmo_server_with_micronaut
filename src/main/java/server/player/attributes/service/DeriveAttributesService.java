package server.player.attributes.service;

import jakarta.inject.Singleton;
import server.attribute.stats.types.AttributeTypes;
import server.player.attributes.model.PlayerAttributes;

import java.util.Map;

@Singleton
public class DeriveAttributesService {

    public boolean derivePhyAmp(PlayerAttributes attributes) {
        String key = AttributeTypes.PHY_AMP.getType();

        return deriveCustomAttribute(attributes, key, AttributeTypes.STR.getType(), 1, 1);
    }

    public boolean deriveMgcAmp(PlayerAttributes attributes) {
        String key = AttributeTypes.MAG_AMP.getType();

        return deriveCustomAttribute(attributes, key, AttributeTypes.INT.getType(), 1, 1);
    }

    public boolean deriveMaxHp(PlayerAttributes attributes) {
        String key = AttributeTypes.MAX_HP.getType();

        return deriveCustomAttribute(attributes, key, AttributeTypes.STR.getType(), 10, 1);
    }

    public boolean deriveCustomAttribute(PlayerAttributes attributes, String key, String baseKey, Integer multiplier, Integer baseDefault) {
        Map<String, Integer> current = attributes.getCurrentAttributes();
        Map<String, Integer> base = attributes.getBaseAttributes();

        Integer value = base.getOrDefault(key, baseDefault);
        value += base.get(baseKey) * multiplier;

        boolean updated = !current.get(key).equals(value);
        base.put(key, value);

        return updated;
    }

}
