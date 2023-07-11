package server.attribute.status.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusTypes {

    UNCONCIOUS("UNCONCIOUS"),
    DEAD("DEAD");
    public final String type;
}
