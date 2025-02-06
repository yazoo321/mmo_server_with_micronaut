package server.combat.service;

import io.micronaut.websocket.WebSocketSession;
import io.netty.util.internal.ConcurrentSet;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.DamageUpdateMessage;
import server.attribute.stats.model.Stats;
import server.attribute.stats.service.PlayerLevelStatsService;
import server.attribute.stats.service.StatsService;
import server.attribute.status.service.StatusService;
import server.combat.model.CombatData;
import server.combat.model.CombatRequest;
import server.combat.repository.CombatDataCache;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Inject
    CombatDataCache combatDataCache;

    Single<Boolean> canEngageCombat(String actorId, String targetId) {
        if (!UUIDHelper.isPlayer(targetId)) {
            // for now if the target is a mob, we can engage
            return Single.just(true);
        }

        return actorHostilityService
                .evaluateActorHostilityStatus(actorId, targetId)
                .map(hostility -> hostility < 5);
    }

    public boolean validatePositionLocation(CombatData combatData, Motion attackerMotion, Motion targetMotion,
                                            int distanceThreshold, WebSocketSession session) {

        if (targetMotion == null) {
            log.error("Target motion is null: this is unexpected, target should be removed from active list");
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
                combatDataCache.cacheCombatData(combatData.getActorId(), combatData);

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

    public boolean validatePositionLocation(
            CombatData combatData,
            Motion attackerMotion,
            String target,
            int distanceThreshold,
            WebSocketSession session) {

        Motion targetMotion =
                actorMotionRepository
                        .fetchActorMotion(target)
                        .doOnError(err -> log.error(err.getMessage()))
                        .blockingGet();

        return validatePositionLocation(combatData, attackerMotion, targetMotion, distanceThreshold, session);
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

    public void handleActorDeath(DamageUpdateMessage damageUpdateMessage) {
        Stats originStats = damageUpdateMessage.getOriginStats();
        Stats targetStats = damageUpdateMessage.getTargetStats();

        if (!targetStats.isPlayer()) {
            actorThreatService
                    .resetActorThreat(originStats.getActorId())
                    .delaySubscription(10_000, TimeUnit.MILLISECONDS)
                    .subscribe();
        }

        if (!originStats.isPlayer()) {
            actorThreatService
                    .removeActorThreat(originStats.getActorId(), List.of(targetStats.getActorId()))
                    .delaySubscription(200, TimeUnit.MILLISECONDS)
                    .subscribe();
        }

        combatDataCache.deleteCombatData(targetStats.getActorId());

    }
}
