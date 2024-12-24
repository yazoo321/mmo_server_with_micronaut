package server.socket.model;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.actionbar.model.ActorActionbar;
import server.attribute.stats.model.DamageSource;
import server.attribute.stats.model.Stats;
import server.attribute.status.model.ActorStatus;
import server.combat.model.CombatRequest;
import server.combat.model.ThreatUpdate;
import server.items.inventory.model.response.GenericInventoryData;
import server.items.model.DroppedItem;
import server.monster.server_integration.model.Monster;
import server.motion.dto.PlayerMotion;
import server.player.model.Character;
import server.skills.model.ActorSkills;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Serdeable
public class SocketResponse {

    String messageType;

    Set<String> lostPlayers; // players that will no longer be synced (out of range, etc)
    Set<String> lostMobs;

    Set<String> playerKeys;
    Set<String> mobKeys;

    Map<String, PlayerMotion> playerMotion;
    Map<String, Character> playerData;
    Map<String, Monster> monsters;

    Map<String, DroppedItem> droppedItems;
    Set<String> itemInstanceIds;

    GenericInventoryData inventoryData;
    String error;

    CombatRequest combatRequest;

    Stats stats;

    ActorStatus actorStatus;

    DamageSource damageSource;

    ActorSkills actorSkills;

    List<ActorActionbar> actionbarList;

    ThreatUpdate threatUpdate;

    String customData;

    public static SocketResponse messageWithType(SocketResponseType type) {
        return SocketResponse.builder().messageType(type.getType()).build();
    }

    public static SocketResponse messageWithTypeAndCustomPayload(SocketResponseType type, String msg) {
        SocketResponse resp = messageWithType(type);
        resp.setCustomData(msg);
        return resp;
    }
}
