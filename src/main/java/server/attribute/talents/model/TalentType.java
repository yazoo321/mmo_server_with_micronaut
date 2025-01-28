package server.attribute.talents.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TalentType {
    
    PASSIVE("PASSIVE"),
    ACTIVE("ACTIVE"),
    AUGMENT("AUGMENT");

    public final String type;

}
