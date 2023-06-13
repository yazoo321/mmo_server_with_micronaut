package server.socket.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageType {
    CREATE_MOB("CREATE_MOB"),
    MOB_MOTION("MOB_MOTION"),
    PLAYER_MOTION("PLAYER_MOTION"),
    MOB_COMBAT("MOB_COMBAT"),
    PLAYER_COMBAT("PLAYER_COMBAT"),
    PICKUP_ITEM("PICKUP_ITEM"),

    DROP_ITEM("DROP_ITEM");

    public final String type;
}
