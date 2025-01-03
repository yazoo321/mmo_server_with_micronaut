package server.combat.service;

import io.micronaut.websocket.WebSocketSession;
import io.netty.util.internal.ConcurrentSet;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.stats.service.PlayerLevelStatsService;
import server.attribute.stats.service.StatsService;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.derived.Dead;
import server.attribute.status.service.StatusService;
import server.attribute.status.types.StatusTypes;
import server.combat.model.CombatData;
import server.combat.model.CombatRequest;
import server.common.dto.Motion;
import server.common.uuid.UUIDHelper;
import server.faction.service.ActorHostilityService;
import server.items.equippable.service.EquipItemService;
import server.monster.server_integration.service.MobInstanceService;
import server.motion.repository.ActorMotionRepository;
import server.session.SessionParamHelper;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseType;
import server.socket.service.WebsocketClientUpdatesService;

@Slf4j
@Singleton
public class CombatService {

    final ConcurrentSet<String> sessionsInCombat = new ConcurrentSet<>();

    @Inject MobInstanceService mobInstanceService;

    @Inject SessionParamHelper sessionParamHelper;

    @Inject StatsService statsService;

    @Inject StatusService statusService;

    @Inject WebsocketClientUpdatesService clientUpdatesService;

    @Inject ActorMotionRepository actorMotionRepository;

    @Inject EquipItemService equipItemService;

    @Inject PlayerLevelStatsService playerLevelStatsService;

    @Inject ActorHostilityService actorHostilityService;

    @Inject ActorThreatService actorThreatService;

    Single<Boolean> canEngageCombat(String actorId, String targetId) {
        if (!UUIDHelper.isPlayer(targetId)) {
            // for now if the target is a mob, we can engage
            return Single.just(true);
        }

        return actorHostilityService
                .evaluateActorHostilityStatus(actorId, targetId)
                .map(hostility -> hostility < 5);
    }

    boolean validatePositionLocation(
            CombatData combatData,
            Motion attackerMotion,
            String target,
            int distanceThreshold,
            WebSocketSession session) {
        // TODO: Refactor mob/player motion calls
        // TODO: Make async

        Motion targetMotion =
                actorMotionRepository
                        .fetchActorMotion(target)
                        .doOnError(err -> log.error(err.getMessage()))
                        .blockingGet();

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
                    clientUpdatesService.sendToSelf(
                            session,
                            SocketResponse.messageWithType(SocketResponseType.COMBAT_TOO_FAR));
                    return false;
                }
                clientUpdatesService.sendToSelf(
                        session,
                        SocketResponse.messageWithType(SocketResponseType.COMBAT_NOT_FACING));
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
                .collect(Collectors.toList());
    }

    void requestSessionsToSwingWeapon(String itemInstanceId, String actorId) {
        CombatRequest request = new CombatRequest();

        request.setItemInstanceId(itemInstanceId);
        request.setActorId(actorId);

        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.INITIATE_ATTACK.getType())
                        .combatRequest(request)
                        .build();

        clientUpdatesService.sendUpdateToListeningIncludingSelf(socketResponse, actorId);
    }

    public void handleActorDeath(Stats targetStats, String actorId) {
        if (targetStats.getDerived(StatsTypes.CURRENT_HP) > 0.0) {
            return;
        }

        statsService
                .getStatsFor(actorId)
                .doOnError(err -> log.error("Failed to get stats, {}", err.getMessage()))
                .doOnSuccess(actorStats -> handleActorDeath(targetStats, actorStats))
                .subscribe();
    }

    public void handleActorDeath(Stats stats, Stats killerStats) {
        if (stats.getDerived(StatsTypes.CURRENT_HP) > 0.0) {
            return;
        }

        // TODO: make async
        ActorStatus statuses = statusService.getActorStatus(stats.getActorId()).blockingGet();
        statuses.aggregateStatusEffects();
        if (statuses.getStatusEffects().contains(StatusTypes.DEAD.getType())) {
            log.info("actor already dead");
            return;
        }

        if (killerStats.isPlayer()) {
            playerLevelStatsService.handleAddXp(stats, killerStats);
        }

        if (stats.isPlayer()) {
            if (!killerStats.isPlayer()) {
                actorThreatService
                        .removeActorThreat(killerStats.getActorId(), List.of(stats.getActorId()))
                        .delaySubscription(200, TimeUnit.MILLISECONDS)
                        .subscribe();
            }
            statusService
                    .removeAllStatuses(stats.getActorId())
                    .doOnSuccess(
                            status -> statusService.addStatusToActor(status, Set.of(new Dead())))
                    .doOnError(er -> log.error(er.getMessage()))
                    .subscribe();

        } else {
            mobInstanceService.handleMobDeath(stats);
            sessionParamHelper.setSharedActorCombatData(stats.getActorId(), null);
        }
    }
}
