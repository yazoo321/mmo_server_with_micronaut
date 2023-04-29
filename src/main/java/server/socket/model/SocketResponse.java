package server.socket.model;

import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.monster.server_integration.model.Monster;
import server.motion.dto.PlayerMotion;
import server.player.character.dto.Character;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocketResponse {

    String messageType;

    Set<String> newPlayers; // players that will be synced soon
    Set<String> lostPlayers; // players that will no longer be synced (out of range, etc)

    Map<String, PlayerMotion> playerMotion;
    Map<String, Character> playerData;
    Map<String, Monster> monsters;

    // other data to be added, e.g. inventory updates
}
