package server.combat.service;

import io.micronaut.websocket.WebSocketSession;
import io.netty.util.internal.ConcurrentSet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.stats.service.StatsService;
import server.attribute.stats.types.StatsTypes;
import server.combat.model.CombatData;
import server.combat.model.CombatRequest;
import server.common.dto.Motion;
import server.monster.server_integration.service.MobInstanceService;
import server.motion.service.PlayerMotionService;
import server.session.SessionParamHelper;
import server.socket.service.ClientUpdatesService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class CombatService {

    final ConcurrentSet<String> sessionsInCombat = new ConcurrentSet<>();

    @Inject
    MobInstanceService mobInstanceService;

    @Inject SessionParamHelper sessionParamHelper;

    @Inject
    StatsService statsService;

    @Inject PlayerMotionService playerMotionService;

    @Inject
    ClientUpdatesService clientUpdatesService;

    boolean validatePositionLocation(CombatData combatData, Motion attackerMotion, String target, int distanceThreshold, WebSocketSession session) {
        // TODO: Refactor mob/player motion calls
        // TODO: Make async

        Motion targetMotion = sessionParamHelper.getSharedActorMotion(target);

        if (targetMotion == null) {
            combatData.getTargets().remove(target);

            return false;
        }

        boolean inRange = attackerMotion.withinRange(targetMotion, distanceThreshold);
        boolean facingTarget = attackerMotion.facingMotion(targetMotion);

        if (!inRange || !facingTarget) {
            if (session == null) {
                return false;
            }
            if (combatData.getLastHelperNotification() == null
                    || Instant.now().getEpochSecond()
                    - combatData.getLastHelperNotification().getEpochSecond()
                    > 3) {
                combatData.setLastHelperNotification(Instant.now());
                sessionParamHelper.setSharedActorCombatData(combatData.getActorId(), combatData);

                if (!inRange) {
                    clientUpdatesService.notifySessionCombatTooFar(session);
                    return false;
                }
                clientUpdatesService.notifySessionCombatNotFacing(session);
            }
            return false;
        }

        return true;
    }

    List<Stats> getTargetStats(Set<String> actors) {
        // TODO: Make async, get from cache
        if (actors.isEmpty()) {
            return new ArrayList<>();
        }
        return actors.stream()
                .map(actor -> statsService.getStatsFor(actor).blockingGet())
                .filter(s -> s.getDerivedStats().get(StatsTypes.CURRENT_HP.getType()) > 0)
                .collect(Collectors.toList());
    }

    void requestSessionsToSwingWeapon(String itemInstanceId, String actorId) {
        CombatRequest request = new CombatRequest();

        request.setItemInstanceId(itemInstanceId);
        request.setActorId(actorId);

        clientUpdatesService.sendAttackAnimUpdates(request);
    }

    void handleActorDeath(Stats stats) {
        if (stats.isPlayer()) {
            // TODO: implement player death
            statsService.addHealth(stats, 300.0);
        } else {
            statsService
                    .deleteStatsFor(stats.getActorId())
                    .doOnError(
                            err ->
                                    log.error(
                                            "Failed to delete stats on death, {}",
                                            err.getMessage()))
                    .subscribe();
            mobInstanceService.handleMobDeath(stats.getActorId());
            sessionParamHelper.setSharedActorCombatData(stats.getActorId(), null);
            clientUpdatesService.notifyServerOfRemovedMobs(Set.of(stats.getActorId()));
        }
    }

}
