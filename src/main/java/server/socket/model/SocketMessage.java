package server.socket.model;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.actionbar.model.ActorActionbar;
import server.combat.model.CombatRequest;
import server.items.inventory.model.response.GenericInventoryData;
import server.monster.server_integration.model.Monster;
import server.motion.dto.PlayerMotion;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Serdeable
public class SocketMessage {

    String updateType;
    Monster monster;
    PlayerMotion playerMotion;

    String mobId; // mob id used by server to identify mesh etc to use
    String actorId; // referring to mob instance or player instance

    String serverName;

    GenericInventoryData inventoryRequest;

    CombatRequest combatRequest;

    ActorActionbar actorActionbar;
}
