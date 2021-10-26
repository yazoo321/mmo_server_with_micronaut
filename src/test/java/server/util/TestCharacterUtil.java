package server.util;

import server.common.dto.Motion;
import server.player.character.dto.Character;

import java.util.Map;

public class TestCharacterUtil {


    public static Character getBasicTestCharacter(String username, String characterName,
                                                  Map<String, String> appearanceInfo) {
        Character character = new Character();
        character.setName(characterName);
        character.setAccountName(username);
        character.setXp(0);
        character.setAppearanceInfo(appearanceInfo);
        character.setIsOnline(false);

        // default motion
        character.setMotion(new Motion(0,0,112,0,0,0,0,0,0, false));

        return character;
    }
}
