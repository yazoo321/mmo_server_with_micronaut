package server.common.attributes.service;

import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import server.attribute.stats.types.StatsTypes;

@Singleton
public class AttributeService {

    public static void validateAttributes(Map<String, Integer> attributes) {
        List<String> invalidAttributes =
                attributes.keySet().stream()
                        .filter(
                                attribute -> {
                                    try {
                                        StatsTypes.valueOf(attribute);
                                        return true;
                                    } catch (IllegalArgumentException ex) {
                                        return false;
                                    }
                                })
                        .toList();
        if (!invalidAttributes.isEmpty()) {
            throw new IllegalArgumentException("Invalid attribute types: " + invalidAttributes);
        }
    }
}
