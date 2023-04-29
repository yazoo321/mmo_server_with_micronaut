package server.socket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.monster.server_integration.model.Monster;
import server.motion.dto.PlayerMotion;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocketMessage {

    String updateType;
    Monster monster;
    PlayerMotion playerMotion;

    String mobInstanceId;

    String playerName;
    String serverName;
}
