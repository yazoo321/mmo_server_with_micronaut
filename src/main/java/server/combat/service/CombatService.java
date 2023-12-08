package server.combat.service;

import io.micronaut.websocket.WebSocketSession;
import io.netty.util.internal.ConcurrentSet;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.stats.service.StatsService;
import server.attribute.stats.types.StatsTypes;
import server.combat.combatInterface.CombatInterface;
import server.combat.model.CombatData;
import server.combat.model.CombatRequest;
import server.common.dto.Motion;
import server.monster.server_integration.model.Monster;
import server.monster.server_integration.service.MobInstanceService;
import server.motion.dto.PlayerMotion;
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

        List<Monster> mobs = mobInstanceService.getMobsByIds(Set.of(target)).blockingGet();
        List<PlayerMotion> players = playerMotionService.getPlayersMotion(Set.of(target)).blockingGet();

        if (mobs.isEmpty() && players.isEmpty()) {
            combatData.getTargets().remove(target);

            return false;
        }
        Motion targetMotion;

        if (!mobs.isEmpty()) {
            targetMotion = mobs.get(0).getMotion();
        } else {
            targetMotion = players.get(0).getMotion();
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
        // TODO: Make async
        if (actors.isEmpty()) {
            return new ArrayList<>();
        }
        return actors.stream()
                .map(actor -> statsService.getStatsFor(actor).blockingGet())
                .filter(s -> s.getDerivedStats().get(StatsTypes.CURRENT_HP.getType()) > 0)
                .collect(Collectors.toList());
    }

    void requestSessionsToSwingWeapon(WebSocketSession session, String itemInstanceId, String actorId) {
        CombatRequest request = new CombatRequest();

        request.setItemInstanceId(itemInstanceId);
        request.setActorId(actorId);

        clientUpdatesService.sendAttackAnimUpdates(request);
    }

}
