package server.socket.model.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SkillMessageType {
    FETCH_ACTIONBAR("FETCH_ACTIONBAR"),
    UPDATE_ACTIONBAR("UPDATE_ACTIONBAR"),
    FETCH_SKILLS("FETCH_SKILLS"),
    START_CHANNELLING("START_CHANNELLING"),
    STOP_CHANNELLING("STOP_CHANNELLING"),
    INITIATE_SKILL("INITIATE_SKILL");

    public final String type;
}
