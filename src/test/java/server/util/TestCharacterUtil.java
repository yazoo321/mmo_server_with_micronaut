package server.util;

import java.util.Map;
import server.player.model.Character;

public class TestCharacterUtil {

    public static Character getBasicTestCharacter(
            String username, String characterName, Map<String, String> appearanceInfo) {
        Character character = new Character();
        character.setName(characterName);
        character.setAccountName(username);
        character.setAppearanceInfo(appearanceInfo);
        character.setIsOnline(false);

        return character;
    }
}
