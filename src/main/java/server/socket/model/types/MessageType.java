package server.socket.model.types;

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
    FETCH_STATUS("FETCH_STATUS"),
    TRY_ATTACK("TRY_ATTACK"),
    STOP_ATTACK("STOP_ATTACK"),
    SET_SESSION_ID("SET_SESSION_ID"),

    UPDATE_ACTOR_SKILLS("UPDATE_ACTOR_SKILLS"),

    ADD_STAT("ADD_STAT"),

    MOVE_ITEM("MOVE_ITEM"),

    ADD_THREAT("ADD_THREAT"),
    RESET_THREAT("RESET_THREAT"),

    RESPAWN_PLAYER("RESPAWN_PLAYER");

    public final String type;
}
