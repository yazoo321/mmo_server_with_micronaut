package server.player.character.service;

import server.common.dto.Motion;
import server.player.character.dto.AccountCharactersResponse;
import server.player.character.dto.Character;
import server.player.character.dto.CreateCharacterRequest;
import server.player.character.repository.PlayerCharacterRepository;
import server.player.motion.dto.PlayerMotion;
import server.player.motion.service.PlayerMotionService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class PlayerCharacterService {

    @Inject
    PlayerCharacterRepository playerCharacterRepository;

    @Inject
    PlayerMotionService playerMotionService;

    public AccountCharactersResponse getAccountCharacters(String username) {
        AccountCharactersResponse accountCharactersResponse = new AccountCharactersResponse();
        accountCharactersResponse.setAccountCharacters(
                playerCharacterRepository.findByAccount(username).blockingGet());
        return accountCharactersResponse;
    }

    public Character createCharacter(CreateCharacterRequest createCharacterRequest, String username) {
        Character newCharacter = new Character();
        newCharacter.setXp(0);
        newCharacter.setName(createCharacterRequest.getName());
        newCharacter.setAccountName(username);
        newCharacter.setAppearanceInfo(createCharacterRequest.getAppearanceInfo());

        // this will throw if there's a duplicate entry
        newCharacter = playerCharacterRepository.createNew(newCharacter).blockingGet();

        PlayerMotion defaultMotion = new PlayerMotion();
        defaultMotion.setPlayerName(newCharacter.getName());
        defaultMotion.setMotion(new Motion(0,0,112,0,0,0,0,0,0));
        playerMotionService.updatePlayerState(defaultMotion);

        return newCharacter;
    }

    // TODO: Support deletes
}
