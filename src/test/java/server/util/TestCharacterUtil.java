package server.util;

import server.player.model.Character;

import java.util.Map;

public class TestCharacterUtil {

    public static Character getBasicTestCharacter(
            String username, String actorId, Map<String, String> appearanceInfo) {
        Character character = new Character();
        character.setName(actorId);
        character.setAccountName(username);
        character.setAppearanceInfo(appearanceInfo);
        character.setIsOnline(false);

        return character;
    }
}
