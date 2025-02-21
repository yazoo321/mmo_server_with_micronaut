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
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import server.actionbar.service.ActionbarService;
import server.attribute.talents.service.TalentService;
import server.combat.service.ActorThreatService;
import server.combat.service.MobCombatService;
import server.combat.service.PlayerCombatService;
import server.motion.dto.PlayerMotion;
import server.playfab.model.PlayFabAuthResponse;
import server.playfab.service.PlayfabService;
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
import server.socket.service.integrations.motion.PlayerMotionIntegration;
import server.socket.service.integrations.status.StatusSocketIntegration;

@Slf4j
@Singleton
public class SocketProcessOutgoingService {
    // this service determines what happens with the outgoing message - specifically where it gets
    // sent

    @Inject UpdateProducer updateProducer;

    @Inject ItemSocketIntegration itemSocketIntegration;

    @Inject StatsSocketIntegration attributeSocketIntegration;

    @Inject StatusSocketIntegration statusSocketIntegration;

    @Inject PlayerMotionIntegration playerMotionIntegration;

    @Inject PlayerCombatService playerCombatService;

    @Inject CombatSkillsService combatSkillsService;

    @Inject TalentService talentService;

    @Inject MobCombatService mobCombatService;

    @Inject ActionbarService actionbarService;

    @Inject UdpSessionCache sessionCache;

    @Inject SessionParamHelper sessionParamHelper;

    @Inject ActorThreatService threatService;

    @Inject PlayfabService playfabService;

    Map<String, BiConsumer<SocketMessage, WebSocketSession>> functionMap;

    Map<String, Consumer<SocketMessage>> udpFunctionMap;

    private final ConcurrentMap<String, WebSocketSession> actorSessions = new ConcurrentHashMap<>();

    public ConcurrentMap<String, WebSocketSession> getLiveSessions() {
        return actorSessions;
    }

    @PostConstruct
    void syncUp() {
        sessionParamHelper.setLiveSessions(getLiveSessions());
    }

    public void removeActorSession(String actorId) {
        actorSessions.remove(actorId);
    }

    public SocketProcessOutgoingService() {
        this.functionMap = new HashMap<>();
        this.functionMap.put(MessageType.PLAYER_MOTION.getType(), this::handlePlayerMotionUpdate);
        this.functionMap.put(MessageType.CREATE_MOB.getType(), this::handleCreateMob);
        this.functionMap.put(MessageType.MOB_MOTION.getType(), this::handleMobMotionUpdate);
        this.functionMap.put(MessageType.PICKUP_ITEM.getType(), this::handlePickupItem);
        this.functionMap.put(MessageType.DROP_ITEM.getType(), this::handleDropItem);
        this.functionMap.put(MessageType.FETCH_INVENTORY.getType(), this::handleFetchInventory);
        this.functionMap.put(MessageType.FETCH_EQUIPPED.getType(), this::handleFetchEquipped);
        this.functionMap.put(MessageType.EQUIP_ITEM.getType(), this::handleEquipItem);
        this.functionMap.put(MessageType.UN_EQUIP_ITEM.getType(), this::handleUnEquipItem);
        this.functionMap.put(MessageType.FETCH_STATS.getType(), this::handleFetchStats);
        this.functionMap.put(MessageType.FETCH_STATUS.getType(), this::handleFetchStatus);
        this.functionMap.put(MessageType.TRY_ATTACK.getType(), this::handleTryAttack);
        this.functionMap.put(MessageType.STOP_ATTACK.getType(), this::handleStopAttack);
        this.functionMap.put(MessageType.SET_SESSION_ID.getType(), this::setSessionId);
        this.functionMap.put(SkillMessageType.INITIATE_SKILL.getType(), this::handleTryStartSkill);
        // skill management
        this.functionMap.put(
                SkillMessageType.FETCH_ALL_SKILLS.getType(), this::handleFetchAllSkills);
        this.functionMap.put(
                SkillMessageType.FETCH_LEARNED_SKILLS.getType(), this::handleFetchLearnedSkills);
        this.functionMap.put(
                SkillMessageType.FETCH_AVAILABLE_SKILLS.getType(),
                this::handleFetchAvailableSkills);
        this.functionMap.put(SkillMessageType.LEARN_SKILL.getType(), this::handleLearnSkill);
        // talent management
        this.functionMap.put(MessageType.FETCH_ALL_TALENTS.getType(), this::handleFetchAllTalents);
        this.functionMap.put(
                MessageType.FETCH_LEARNED_TALENTS.getType(), this::handleFetchLearnedTalents);
        this.functionMap.put(
                MessageType.FETCH_AVAILABLE_TALENTS.getType(), this::handleAvailableTalents);
        this.functionMap.put(MessageType.LEARN_TALENT.getType(), this::handleLearnTalent);

        this.functionMap.put(
                SkillMessageType.FETCH_ACTIONBAR.getType(), this::handleFetchActionBar);
        this.functionMap.put(
                SkillMessageType.UPDATE_ACTIONBAR.getType(), this::handleUpdateActionBar);
        this.functionMap.put(MessageType.ADD_STAT.getType(), this::handleAddStat);
        this.functionMap.put(MessageType.MOVE_ITEM.getType(), this::handleMoveItem);
        this.functionMap.put(MessageType.RESPAWN_PLAYER.getType(), this::handleRespawnPlayer);
        this.functionMap.put(MessageType.ADD_THREAT.getType(), this::handleAddThreat);
        this.functionMap.put(MessageType.RESET_THREAT.type, this::handleResetThreat);

        this.udpFunctionMap =
                new HashMap<>(
                        Map.of(
                                MessageType.PLAYER_MOTION.getType(), this::handlePlayerMotionUpdate,
                                MessageType.MOB_MOTION.getType(), this::handleMobMotionUpdate));
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

        //        log.info("sending player motion update");
        updateProducer.sendPlayerMotionUpdate(message.getPlayerMotion());
    }

    private void handleMobMotionUpdate(SocketMessage message) {
        updateProducer.sendMobMotionUpdate(message.getMonster());
    }

    private void handleMobMotionUpdate(SocketMessage message, WebSocketSession session) {
        handleMobMotionUpdate(message);
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
        itemSocketIntegration.handleFetchInventory(message.getActorId(), session);
    }

    private void handleFetchEquipped(SocketMessage message, WebSocketSession session) {
        itemSocketIntegration.handleFetchEquipped(message.getActorId(), session);
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

    private void handleFetchStatus(SocketMessage message, WebSocketSession session) {
        statusSocketIntegration.handleFetchActorStatus(message.getActorId(), session);
    }

    private void handleAddStat(SocketMessage message, WebSocketSession session) {
        attributeSocketIntegration.handleAddBaseStat(message, session);
    }

    private void handleMoveItem(SocketMessage message, WebSocketSession session) {
        itemSocketIntegration.handleMoveItem(message.getInventoryRequest(), session);
    }

    private void handleRespawnPlayer(SocketMessage message, WebSocketSession session) {
        playerMotionIntegration.handlePlayerRespawn(message, session);
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

    private void handleFetchAllSkills(SocketMessage message, WebSocketSession session) {
        combatSkillsService.fetchAllSkills(session);
    }

    private void handleFetchLearnedSkills(SocketMessage message, WebSocketSession session) {
        String actorId =
                SessionParamHelper.getIsPlayer(session)
                        ? SessionParamHelper.getActorId(session)
                        : message.getActorId();
        combatSkillsService.fetchActorTrainedSkills(actorId, session);
    }

    private void handleFetchAvailableSkills(SocketMessage message, WebSocketSession session) {
        String actorId =
                SessionParamHelper.getIsPlayer(session)
                        ? SessionParamHelper.getActorId(session)
                        : message.getActorId();
        combatSkillsService.fetchAvailableSkillsToLevel(actorId, session);
    }

    private void handleLearnSkill(SocketMessage message, WebSocketSession session) {
        combatSkillsService.handleLearnSkill(message.getCustomData(), session);
    }

    private void handleFetchAllTalents(SocketMessage message, WebSocketSession session) {
        talentService.fetchAllTalentsForTree(session, message.getCustomData());
    }

    private void handleFetchLearnedTalents(SocketMessage message, WebSocketSession session) {
        String actorId =
                SessionParamHelper.getIsPlayer(session)
                        ? SessionParamHelper.getActorId(session)
                        : message.getActorId();
        talentService.fetchActorLearnedTalents(actorId, session);
    }

    private void handleAvailableTalents(SocketMessage message, WebSocketSession session) {
        talentService.fetchAvailableTalents(session);
    }

    private void handleLearnTalent(SocketMessage message, WebSocketSession session) {
        // custom data should be populated with "talentName:level", e.g. "Sharpened blades:2"
        String data = message.getCustomData();
        String[] parts = data.split(":");
        String talentName = parts[0];
        int level;
        try {
            level = Integer.parseInt(parts[1]);
        } catch (Exception e) {
            log.error("Error processing input data from learn talent!");
            return;
        }

        if (talentName == null || talentName.isEmpty() || level == 0) {
            log.error("Error processing input data from learn talent!");
            return;
        }

        // specify level to avoid concurrency/race condition issues
        talentService.learnTalent(session, talentName, level);
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

    private void handleAddThreat(SocketMessage socketMessage, WebSocketSession session) {
        if (!SessionParamHelper.getIsServer(session)) {
            return;
        }
        threatService
                .addActorThreat(socketMessage.getActorId(), socketMessage.getCustomData(), 100)
                .doOnError(
                        err ->
                                log.error(
                                        "Failed to add threat in socket process outgoing service,"
                                                + " {}",
                                        err.getMessage()))
                .subscribe();
    }

    private void handleResetThreat(SocketMessage socketMessage, WebSocketSession session) {
        if (!SessionParamHelper.getIsServer(session)) {
            return;
        }
        threatService
                .resetActorThreat(socketMessage.getActorId())
                .doOnError(
                        err ->
                                log.error(
                                        "Failed to reset threat from socket request, {}",
                                        err.getMessage()))
                .onErrorComplete()
                .subscribe();
    }

    private void handleCharacterRespawn(SocketMessage socketMessage, WebSocketSession session) {}

    private String authenticatePlayer(SocketMessage message, WebSocketSession session) {
        String data = message.getActorId();

        if (data == null || data.isEmpty()) {
            log.error("custom data on set session id is empty!");
            session.close();
            throw new InvalidParameterException("Session ID not set correctly");
        }
        String[] parts = data.split("-:-");
        if (parts.length < 3) {
            log.error("session params not set correctly");
            session.close();
            throw new InvalidParameterException("Session ID not set correctly");
        }

        String token = parts[0];
        String playFabId = parts[1];
        String actorId = parts[2];

        try {
            PlayFabAuthResponse resp = playfabService.validateSessionTicket(token);
            if (!resp.getPlayFabId().equals(playFabId)) {
                log.error("Authentication with playfab not successful");
                session.close();
            } else {
                log.info("Successfully authenticated with Playfab, account id: {}", playFabId);
            }
        } catch (Exception e) {
            log.error("Caught unhandled exception while validating session");
            session.close();
        }

        // TODO: validate the actor ID belongs to the account.
        return actorId;
    }

    private void setSessionId(SocketMessage message, WebSocketSession session) {
        // custom data is used to send the port; re-using actorId field to get all auth info
        String serverName =
                message.getServerName() == null || message.getServerName().isBlank()
                        ? null
                        : message.getServerName();

        String actorId = "";
        if (message.getActorId() != null && !message.getActorId().isBlank()) {
            // this is a player login, require to authenticate. (skip auth on server)
            actorId = authenticatePlayer(message, session);
        }

        //        String actorId =
        //                message.getActorId() == null || message.getActorId().isBlank()
        //                        ? null
        //                        : message.getActorId();

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
