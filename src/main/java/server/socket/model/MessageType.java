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
    FETCH_INVENTORY("FETCH_INVENTORY"),
    FETCH_EQUIPPED("FETCH_EQUIPPED"),
    DROP_ITEM("DROP_ITEM"),
    EQUIP_ITEM("EQUIP_ITEM"),
    UN_EQUIP_ITEM("UN_EQUIP_ITEM"),
    FETCH_STATS("FETCH_STATS"),
    TRY_ATTACK("TRY_ATTACK");

    public final String type;
}
