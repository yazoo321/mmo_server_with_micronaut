package server.socket.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SocketMessagesToPlayer {
    COMBAT_TOO_FAR("COMBAT_TOO_FAR"),
    COMBAT_NOT_FACING("COMBAT_NOT_FACING"),
    SKILL_ON_CD("SKILL_ON_CD");

    public final String type;
}
