package server.faction.service;

import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.Cacheable;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import server.faction.model.ActorAllegiance;
import server.faction.model.HostileAllegiance;

@CacheConfig("actorAllegianceCacheLocal")
@Slf4j
@Singleton
public class ActorHostilityService {
    private static final String HOSTILE_ALLEGIANCE_CACHE = "hostility-cache";

    @Inject ActorAllegianceService actorAllegianceService;

    @Inject HostileAllegianceService hostileAllegianceService;

    @Cacheable(
            value = HOSTILE_ALLEGIANCE_CACHE,
            parameters = {"actorId", "targetId"})
    public Single<Integer> evaluateActorHostilityStatus(String actorId, String targetId) {
        // Get actor allegiance asynchronously
        return actorAllegianceService
                .getActorAllegiance(actorId)
                .flatMap(
                        actorAllegiances -> {
                            // Get target allegiance after actor allegiance is fetched
                            return actorAllegianceService
                                    .getActorAllegiance(targetId)
                                    .flatMap(
                                            targetAllegiances -> {
                                                Set<String> actorList =
                                                        actorAllegiances.stream()
                                                                .map(
                                                                        ActorAllegiance
                                                                                ::getAllegianceName)
                                                                .collect(Collectors.toSet());

                                                Set<String> targetList =
                                                        targetAllegiances.stream()
                                                                .map(
                                                                        ActorAllegiance
                                                                                ::getAllegianceName)
                                                                .collect(Collectors.toSet());

                                                // Fetch hostile allegiances
                                                return hostileAllegianceService
                                                        .getHostilities(new ArrayList<>(actorList))
                                                        .map(
                                                                hostileAllegiances -> {
                                                                    // Filter hostilities to find
                                                                    // the minimum hostility level
                                                                    return hostileAllegiances
                                                                            .stream()
                                                                            .filter(
                                                                                    hostile ->
                                                                                            targetList
                                                                                                    .contains(
                                                                                                            hostile
                                                                                                                    .getHostileTo()))
                                                                            .map(
                                                                                    HostileAllegiance
                                                                                            ::getHostilityLevel)
                                                                            .min(Integer::compare)
                                                                            .orElse(0);
                                                                    // Default to 0 if no hostility found
                                                                });
                                            });
                        })
                .doOnError(
                        err ->
                                log.error(
                                        "Error evaluating hostility status: {}", err.getMessage()));
    }
}
