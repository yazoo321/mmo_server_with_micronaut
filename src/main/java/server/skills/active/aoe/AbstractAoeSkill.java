package server.skills.active.aoe;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.reactivex.rxjava3.core.Single;
import server.common.dto.Location;
import server.skills.active.channelled.ChannelledSkill;
import server.skills.behavior.AoeSkill;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: This is found to be quite inefficient
// Perhaps we should have the client give us a list of the actors that the AOE will hit
// then we validate the actors using cache, rather than requesting for actors within area from DB
public abstract class AbstractAoeSkill extends ChannelledSkill implements AoeSkill {
    @JsonProperty
    Integer diameter;

    @JsonProperty
    Boolean includeCaster;

    public AbstractAoeSkill(
            String name, String description, Map<String, Double> derived,
            int cooldown, int castTime, boolean allowsMovement, boolean canInterrupt,
            int maxRange, int travelSpeed, Map<String, Integer> requirements,
            int diameter, int durationMs, int ticks, boolean includeCaster) {

        super(name, description, derived, cooldown, castTime, allowsMovement, canInterrupt, maxRange, travelSpeed,
                requirements, durationMs, ticks);
        this.diameter = diameter;
        this.includeCaster = includeCaster;
    }


    // TODO: we need to consider allegiance/faction
    // we have a faction service with cache which will tell us if the actors are hostile
    // heals should not affect hostile actors
    // damage effects should not effect friendly actors
    public Single<List<String>> getAffectedActors(Location location, String casterId) {
        Single<List<String>> mobIdsSingle = actorMotionRepository.getNearbyMobs(location, diameter);
        Single<List<String>> playerIdsSingle = actorMotionRepository.getNearbyPlayers(location, diameter);

        return Single.zip(mobIdsSingle, playerIdsSingle, (mobIds, playerIds) -> {
            List<String> allIds = new ArrayList<>(mobIds);
            allIds.addAll(playerIds);

            if (!includeCaster) {
                allIds = allIds.stream().filter(s -> !s.equalsIgnoreCase(casterId)).toList();
            }
            return allIds;
        });
    }

    public Single<List<String>> getAffectedPlayers(Location location, String casterId) {
        return actorMotionRepository.getNearbyPlayers(location, diameter)
                .map(ids -> {
                    if (!includeCaster) {
                        ids = ids.stream().filter(s -> !s.equalsIgnoreCase(casterId)).toList();
                    }
                    return ids;
                });
    }

}
