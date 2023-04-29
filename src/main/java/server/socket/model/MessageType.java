package server.socket.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageType {
    MOB_MOTION("MOB_MOTION"),
    PLAYER_MOTION("PLAYER_MOTION"),
    MOB_COMBAT("MOB_COMBAT"),
    PLAYER_COMBAT("PLAYER_COMBAT"),
    INVENTORY_UPDATE("INVENTORY_UPDATE");

    public final String type;
}
