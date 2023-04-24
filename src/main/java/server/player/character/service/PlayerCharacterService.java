package server.player.character.service;

import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import server.player.attributes.levels.service.PlayerLevelAttributeService;
import server.player.attributes.service.PlayerAttributeService;
import server.player.character.dto.AccountCharactersResponse;
import server.player.character.dto.Character;
import server.player.character.dto.CreateCharacterRequest;
import server.player.character.inventory.service.InventoryService;
import server.player.character.repository.PlayerCharacterRepository;
import server.player.motion.service.PlayerMotionService;

@Slf4j
@Service
public class PlayerCharacterService {

    @Inject PlayerCharacterRepository playerCharacterRepository;

    @Inject InventoryService inventoryService;

    @Inject PlayerAttributeService attributeService;

    @Inject PlayerLevelAttributeService levelAttributeService;

    @Inject PlayerMotionService playerMotionService;

    public AccountCharactersResponse getAccountCharacters(String username) {
        List<Character> characterList = playerCharacterRepository.findByAccount(username);

        return new AccountCharactersResponse(characterList);
    }

    public AccountCharactersResponse getCharacters(List<String> names) {
        List<Character> characterList = playerCharacterRepository.findByName(names);

        return new AccountCharactersResponse(characterList);
    }

    public Character createCharacter(
            CreateCharacterRequest createCharacterRequest, String username) {
        log.info("Creating character: {}", createCharacterRequest.getName());
        createCharacterRequest.setClassName(createCharacterRequest.getClassName().toLowerCase());

        Character newCharacter = new Character();
        newCharacter.setName(createCharacterRequest.getName());
        newCharacter.setAccountName(username);
        newCharacter.setAppearanceInfo(createCharacterRequest.getAppearanceInfo());
        newCharacter.setUpdatedAt(Instant.now());
        newCharacter.setIsOnline(false);

        // this will throw if there's a duplicate entry
        newCharacter = playerCharacterRepository.createNew(newCharacter);

        try {
            // call relevant services to initialise data
            inventoryService.createInventoryForNewCharacter(newCharacter.getName());
            attributeService.createBaseAttributes(newCharacter.getName());
            levelAttributeService.initializeCharacterClass(
                    newCharacter.getName(), createCharacterRequest.getClassName());
            playerMotionService.initializePlayerMotion(newCharacter.getName());
        } catch (Exception e) {
            log.warn("Create character failed, rolling back changes");
            // we need to rollback the changes
            rollbackChanges(newCharacter.getName());

            // give the 'useful' message back
            throw e;
        }

        return newCharacter;
    }

    private void rollbackChanges(String playerName) {
        attributeService.removePlayerAttributes(playerName);
        inventoryService.clearAllDataForCharacter(playerName);
        playerMotionService.deletePlayerMotion(playerName);
        playerCharacterRepository.deleteByCharacterName(playerName);
    }

    // TODO: Support deletes
}
