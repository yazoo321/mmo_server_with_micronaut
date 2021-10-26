package server.player.motion.service;

import com.mongodb.client.result.UpdateResult;
import server.player.character.dto.AccountCharactersResponse;
import server.player.character.dto.Character;
import server.player.character.repository.PlayerCharacterRepository;
import server.player.motion.dto.PlayerMotion;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class PlayerMotionService {

    @Inject
    PlayerCharacterRepository playerCharacterRepository;

    public UpdateResult updatePlayerState(PlayerMotion request) {
        // TODO: make async
        return playerCharacterRepository.updateMotion(request.getMotion(), request.getPlayerName()).blockingGet();
    }

    public AccountCharactersResponse getPlayersNearMe(String playerName) {
        List<Character> characterList = playerCharacterRepository.getPlayersNear(playerName).blockingGet();
        return new AccountCharactersResponse(characterList);
    }
}
