package server.skills.active.aoe.circle;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.reactivex.rxjava3.core.Single;
import server.common.dto.Location;
import server.skills.active.channelled.ChannelledSkill;
import server.skills.behavior.AoeSkill;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class CircleAoeSkill extends ChannelledSkill implements AoeSkill {
    @JsonProperty
    Integer diameter;

    @JsonProperty
    Integer durationMs;

    @JsonProperty
    Integer ticks;

    @JsonProperty
    Boolean includeCaster;

    public CircleAoeSkill(
            String name, String description, Map<String, Double> derived,
            int cooldown, int castTime, boolean allowsMovement, boolean canInterrupt,
            int maxRange, int travelSpeed, Map<String, Integer> requirements,
            int diameter, int durationMs, int ticks, boolean includeCaster) {

        super(name, description, derived, cooldown, castTime, allowsMovement, canInterrupt, maxRange, travelSpeed, requirements);
        this.diameter = diameter;
        this.durationMs = durationMs;
        this.ticks = ticks;
        this.includeCaster = includeCaster;
    }


    public Single<List<String>> getAffectedActors(Location location, String casterId) {
        Single<List<String>> mobIdsSingle = actorMotionRepository.getNearbyMobs(location, diameter);
        Single<List<String>> playerIdsSingle = actorMotionRepository.getNearbyPlayers(location, diameter);

        return Single.zip(mobIdsSingle, playerIdsSingle, (mobIds, playerIds) -> {
            List<String> allIds = new ArrayList<>(mobIds);
            allIds.addAll(playerIds);

            if (!includeCaster) {
                allIds = allIds.stream().filter(s -> s.equalsIgnoreCase(casterId)).toList();
            }
            return allIds;
        });
    }



}
