package server.player.character.service;

import server.player.character.dto.Character;
import server.player.character.repository.PlayerCharacterRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class PlayerCharacterService {

    @Inject
    PlayerCharacterRepository playerCharacterRepository;

    public List<Character> getAccountCharacters(String username) {
        return playerCharacterRepository.findByAccount(username).blockingGet();
    }

    public Character createCharacter(String characterName, String username) {
        Character newCharacter = new Character();
        newCharacter.setXp(0);
        newCharacter.setName(characterName);
        newCharacter.setAccountName(username);

        // this will throw if there's an entry
        return playerCharacterRepository.createNew(newCharacter).blockingGet();
    }

    // we're not going to support deletes for now.
}
