package server.motion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SessionParams {

    LAST_UPDATED_AT("LAST_UPDATED_AT"),
    TRACKING_PLAYERS("TRACKING_PLAYERS"),
    TRACKING_MOBS("TRACKING_MOBS");

    public final String type;

}
