package server.socket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.monster.server_integration.model.Monster;
import server.motion.dto.PlayerMotion;
import server.player.character.inventory.model.response.GenericInventoryData;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocketMessage {

    String updateType;
    Monster monster;
    PlayerMotion playerMotion;

    String mobId; // mob id used by server to identify mesh etc to use
    String mobInstanceId; // mob instance ID is the unique mob identifier

    String playerName;
    String serverName;

    GenericInventoryData inventoryRequest;
}
