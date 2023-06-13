package server.motion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SessionParams {
    LAST_UPDATED_AT("LAST_UPDATED_AT"),
    MOTION("MOTION"),
    TRACKING_PLAYERS("TRACKING_PLAYERS"),
    TRACKING_MOBS("TRACKING_MOBS"),
    PLAYER_NAME("PLAYER_NAME"),
    SERVER_NAME("SERVER_NAME"),
    DROPPED_ITEMS("DROPPED_ITEMS");

    public final String type;
}
