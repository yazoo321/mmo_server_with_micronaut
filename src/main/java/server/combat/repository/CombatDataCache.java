package server.combat.repository;

import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.CacheInvalidate;
import io.micronaut.cache.annotation.CachePut;
import io.micronaut.cache.annotation.Cacheable;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.combat.model.CombatData;

@Slf4j
@Singleton
@CacheConfig("combat-data-cache")
public class CombatDataCache {

    private static final String COMBAT_CACHE = "combat-data-cache";

    @Cacheable(value = COMBAT_CACHE, parameters = "actorId")
    public CombatData fetchCombatData(String actorId) {
        // if this is called, its not cached!
        return new CombatData(actorId);
    }

    @CachePut(value = COMBAT_CACHE, parameters = "actorId")
    public CombatData cacheCombatData(String actorId, CombatData combatData) {
        return combatData;
    }

    @CacheInvalidate(value = COMBAT_CACHE, parameters = "actorId")
    public void deleteCombatData(String actorId) {
        // handled via annotation
    }
}
