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
    // Deprecated by server.player.motion.socket.v1.service.PlayerMotionService

    @Inject
    PlayerCharacterRepository playerCharacterRepository;

    public UpdateResult updatePlayerState(PlayerMotion request) {
        // TODO: make async
        return playerCharacterRepository.updateMotion(request.getMotion(), request.getPlayerName());
    }

    public AccountCharactersResponse getPlayersNearMe(String playerName) {
        List<Character> characterList = playerCharacterRepository.getPlayersNear(playerName);
        return new AccountCharactersResponse(characterList);
    }
}
