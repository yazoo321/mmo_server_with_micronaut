package server.socket.service;

import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;
import server.actionbar.service.ActionbarService;
import server.combat.service.MobCombatService;
import server.combat.service.PlayerCombatService;
import server.motion.dto.PlayerMotion;
import server.session.SessionParamHelper;
import server.session.cache.UdpSessionCache;
import server.skills.service.CombatSkillsService;
import server.socket.model.SocketMessage;
import server.socket.model.UdpAddressHolder;
import server.socket.model.types.MessageType;
import server.socket.model.types.SkillMessageType;
import server.socket.producer.UpdateProducer;
import server.socket.service.integrations.attributes.StatsSocketIntegration;
import server.socket.service.integrations.items.ItemSocketIntegration;
import server.socket.v1.CommunicationSocket;

@Slf4j
@Singleton
public class SocketProcessOutgoingService {
    // this service determines what happens with the outgoing message - specifically where it gets
    // sent

    @Inject UpdateProducer updateProducer;

    @Inject ItemSocketIntegration itemSocketIntegration;

    @Inject StatsSocketIntegration attributeSocketIntegration;

    @Inject PlayerCombatService playerCombatService;

    @Inject CombatSkillsService combatSkillsService;

    @Inject MobCombatService mobCombatService;

    @Inject ActionbarService actionbarService;

    @Inject SessionParamHelper sessionParamHelper;

    @Inject
    UdpSessionCache sessionCache;

    Map<String, BiConsumer<SocketMessage, WebSocketSession>> functionMap;

    Map<String, Consumer<SocketMessage>> udpFunctionMap;

    private final ConcurrentMap<String, WebSocketSession> actorSessions = new ConcurrentHashMap<>();

    public ConcurrentMap<String, WebSocketSession> getLiveSessions() {
        return actorSessions;
    }
    public void removeActorSession(String actorId) {
        actorSessions.remove(actorId);
    }

    public SocketProcessOutgoingService() {
        this.functionMap =
                new HashMap<>(
                        Map.of(
                                MessageType.PLAYER_MOTION.getType(), this::handlePlayerMotionUpdate,
                                MessageType.CREATE_MOB.getType(), this::handleCreateMob,
                                MessageType.MOB_MOTION.getType(), this::handleMobMotionUpdate,
                                MessageType.PICKUP_ITEM.getType(), this::handlePickupItem,
                                MessageType.DROP_ITEM.getType(), this::handleDropItem,
                                MessageType.FETCH_INVENTORY.getType(), this::handleFetchInventory,
                                MessageType.FETCH_EQUIPPED.getType(), this::handleFetchEquipped,
                                MessageType.EQUIP_ITEM.getType(), this::handleEquipItem,
                                MessageType.UN_EQUIP_ITEM.getType(), this::handleUnEquipItem,
                                MessageType.FETCH_STATS.getType(), this::handleFetchStats));
        // map.of supports up to 10 items
        this.functionMap.put(MessageType.TRY_ATTACK.getType(), this::handleTryAttack);
        this.functionMap.put(MessageType.STOP_ATTACK.getType(), this::handleStopAttack);
        this.functionMap.put(MessageType.SET_SESSION_ID.getType(), this::setSessionId);
        this.functionMap.put(SkillMessageType.INITIATE_SKILL.getType(), this::handleTryStartSkill);
        this.functionMap.put(SkillMessageType.FETCH_SKILLS.getType(), this::handleFetchSkills);
        this.functionMap.put(
                SkillMessageType.FETCH_ACTIONBAR.getType(), this::handleFetchActionBar);
        this.functionMap.put(
                SkillMessageType.UPDATE_ACTIONBAR.getType(), this::handleUpdateActionBar);


        this.udpFunctionMap = new HashMap<>(
                Map.of(
                        MessageType.PLAYER_MOTION.getType(), this::handlePlayerMotionUpdate
                )
        );
    }

    public void processMessage(SocketMessage socketMessage, WebSocketSession session) {
        String updateType = socketMessage.getUpdateType();

        if (updateType == null) {
            throw new InvalidParameterException("message type missing");
        }

        if (functionMap.containsKey(updateType)) {
            functionMap.get(updateType).accept(socketMessage, session);
        } else {
            log.error("Did not recognise update type, {}", updateType);
        }
    }

    public void processUDPMessage(SocketMessage socketMessage) {
        String updateType = socketMessage.getUpdateType();

        if (updateType == null) {
            throw new InvalidParameterException("message type missing");
        }

        if (udpFunctionMap.containsKey(updateType)) {
            udpFunctionMap.get(updateType).accept(socketMessage);
        } else {
            log.error("Did not recognise update type, {}", updateType);
        }
    }

    // update motion for player
    private void handlePlayerMotionUpdate(SocketMessage message, WebSocketSession session) {
        handlePlayerMotionUpdate(message);
    }

    private void handlePlayerMotionUpdate(SocketMessage message) {
        PlayerMotion motion = message.getPlayerMotion();

        Map<String, String> validateFields =
                Map.of(
                        "Player name",
                        motion.getActorId(),
                        "b",
                        "i",
                        "Map",
                        motion.getMotion().getMap(),
                        "X co-ordinate",
                        motion.getMotion().getX().toString(),
                        "Y co-ordinate",
                        motion.getMotion().getY().toString(),
                        "Z co-ordinate",
                        motion.getMotion().getZ().toString());

        for (Map.Entry<String, String> entry : validateFields.entrySet()) {
            if (!validate(entry.getKey(), entry.getValue())) {
                return;
            }
        }

        updateProducer.sendPlayerMotionUpdate(message.getPlayerMotion());
    }

    // update motion for monster
    private void handleMobMotionUpdate(SocketMessage message, WebSocketSession session) {
        updateProducer.sendMobMotionUpdate(message.getMonster());
    }

    private void handleCreateMob(SocketMessage message, WebSocketSession session) {
        SessionParamHelper.addTrackingMobs(session, Set.of(message.getActorId()));
        updateProducer.sendCreateMob(message.getMonster());
    }

    // handle inventory interaction
    private void handlePickupItem(SocketMessage message, WebSocketSession session) {
        itemSocketIntegration.handlePickupItem(message.getInventoryRequest(), session);
    }

    private void handleDropItem(SocketMessage message, WebSocketSession session) {
        itemSocketIntegration.handleDropItem(message.getInventoryRequest(), session);
    }

    private void handleFetchInventory(SocketMessage message, WebSocketSession session) {
        itemSocketIntegration.handleFetchInventory(message.getInventoryRequest(), session);
    }

    private void handleFetchEquipped(SocketMessage message, WebSocketSession session) {
        itemSocketIntegration.handleFetchEquipped(message.getInventoryRequest(), session);
    }

    private void handleEquipItem(SocketMessage message, WebSocketSession session) {
        itemSocketIntegration.handleEquipItem(message.getInventoryRequest(), session);
    }

    private void handleUnEquipItem(SocketMessage message, WebSocketSession session) {
        itemSocketIntegration.handleUnEquipItem(message.getInventoryRequest(), session);
    }

    private void handleFetchStats(SocketMessage message, WebSocketSession session) {
        String actorId = message.getActorId();
        attributeSocketIntegration.handleFetchStats(actorId, session);
    }

    private void handleTryAttack(SocketMessage message, WebSocketSession session) {
        if (SessionParamHelper.getIsPlayer(session)) {
            playerCombatService.requestAttack(session, message.getCombatRequest());
        } else {
            mobCombatService.requestAttack(message.getCombatRequest());
        }
    }

    private void handleTryStartSkill(SocketMessage message, WebSocketSession session) {
        combatSkillsService.tryApplySkill(message.getCombatRequest(), session);
    }

    private void handleFetchSkills(SocketMessage message, WebSocketSession session) {
        String actorId =
                SessionParamHelper.getIsPlayer(session)
                        ? SessionParamHelper.getActorId(session)
                        : message.getActorId();
        combatSkillsService.getActorAvailableSkills(actorId, session);
    }

    private void handleStopAttack(SocketMessage message, WebSocketSession session) {
        if (SessionParamHelper.getIsPlayer(session)) {
            playerCombatService.requestStopAttack(SessionParamHelper.getActorId(session));
        } else {
            mobCombatService.requestStopAttack(message.getActorId());
        }
    }

    private void handleFetchActionBar(SocketMessage message, WebSocketSession session) {
        actionbarService.getActorActionbar(session);
    }

    private void handleUpdateActionBar(SocketMessage socketMessage, WebSocketSession session) {
        actionbarService.updateActionbarItem(socketMessage.getActorActionbar());
    }

    private void setSessionId(SocketMessage message, WebSocketSession session) {
        String serverName =
                message.getServerName() == null || message.getServerName().isBlank()
                        ? null
                        : message.getServerName();
        String actorId =
                message.getActorId() == null || message.getActorId().isBlank()
                        ? null
                        : message.getActorId();

        SessionParamHelper.setServerName(session, serverName);
        SessionParamHelper.setActorId(session, actorId);

        String useId = actorId == null || actorId.isBlank() ? serverName : actorId;

        actorSessions.put(useId, session);

        String address = SessionParamHelper.getAddress(session);
        Integer port = Integer.parseInt(message.getCustomData());

        if (address == null || address.isBlank()) {
            log.error("Address cannot be blank");
            throw new RuntimeException("UDP address not configured");
        }

        sessionCache.setUdpSession(useId, new UdpAddressHolder(address, port));
    }

    private boolean validate(String value, String name) {
        if (!isValid(value)) {
            log.error("{} is not valid in player motion!", name);
            return false;
        }
        return true;
    }

    private boolean isValid(String data) {
        return data != null && !data.isBlank();
    }
}
