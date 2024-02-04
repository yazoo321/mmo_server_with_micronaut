package server.combat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CombatState {
    IDLE("IDLE"),
    ATTACKING("ATTACKING"),
    CHANNELING("CHANNELING");

    public final String type;
}
