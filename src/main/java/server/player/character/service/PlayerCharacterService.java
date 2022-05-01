package server.player.character.service;

import server.common.dto.Motion;
import server.player.character.dto.AccountCharactersResponse;
import server.player.character.dto.Character;
import server.player.character.dto.CreateCharacterRequest;
import server.player.character.repository.PlayerCharacterRepository;
import server.player.motion.service.PlayerMotionService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Singleton
public class PlayerCharacterService {

    @Inject
    PlayerCharacterRepository playerCharacterRepository;

    @Inject
    PlayerMotionService playerMotionService;

    public AccountCharactersResponse getAccountCharacters(String username) {
        List<Character> characterList =  playerCharacterRepository.findByAccount(username);

        return new AccountCharactersResponse(characterList);
    }

    public Character createCharacter(CreateCharacterRequest createCharacterRequest, String username) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        Character newCharacter = new Character();
        newCharacter.setXp(0);
        newCharacter.setName(createCharacterRequest.getName());
        newCharacter.setAccountName(username);
        newCharacter.setAppearanceInfo(createCharacterRequest.getAppearanceInfo());
        newCharacter.setMotion(new Motion(0,0,112,0,0,0,0,0,0, false));
        newCharacter.setUpdatedAt(now);
        newCharacter.setIsOnline(false);

        // this will throw if there's a duplicate entry
        newCharacter = playerCharacterRepository.createNew(newCharacter);

        return newCharacter;
    }

    // TODO: Support deletes
}
