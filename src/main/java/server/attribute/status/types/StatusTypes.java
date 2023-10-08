package server.attribute.status.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusTypes {

    UNCONCIOUS("UNCONCIOUS"),
    DEAD("DEAD"),
    STUNNED("STUNNED"),
    SILENCED("SILENCED");
    public final String type;
}
