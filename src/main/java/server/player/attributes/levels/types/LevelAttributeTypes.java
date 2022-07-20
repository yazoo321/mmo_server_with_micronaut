package server.player.attributes.levels.types;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum LevelAttributeTypes {
    XP("XP"),
    LEVEL("LEVEL");




    public final String type;
}