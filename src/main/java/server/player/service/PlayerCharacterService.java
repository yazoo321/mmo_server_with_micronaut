package server.player.service;

import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.service.StatsService;
import server.items.inventory.service.InventoryService;
import server.motion.service.PlayerMotionService;
import server.player.attributes.levels.service.PlayerLevelAttributeService;
import server.player.attributes.service.PlayerAttributeService;
import server.player.model.AccountCharactersResponse;
import server.player.model.Character;
import server.player.model.CreateCharacterRequest;
import server.player.repository.PlayerCharacterRepository;

@Slf4j
@Singleton
public class PlayerCharacterService {

    @Inject PlayerCharacterRepository playerCharacterRepository;

    @Inject InventoryService inventoryService;

    @Inject PlayerAttributeService attributeService;

    @Inject PlayerLevelAttributeService levelAttributeService;

    @Inject PlayerMotionService playerMotionService;

    @Inject StatsService statsService;

    public AccountCharactersResponse getAccountCharacters(String username) {
        List<Character> characterList = playerCharacterRepository.findByAccount(username);

        return new AccountCharactersResponse(characterList);
    }

    public AccountCharactersResponse getCharacters(List<String> names) {
        List<Character> characterList = playerCharacterRepository.findByName(names);

        return new AccountCharactersResponse(characterList);
    }

    public Single<List<Character>> getCharactersByNames(Set<String> playerNames) {
        return playerCharacterRepository.findByNames(playerNames);
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
            statsService.initializePlayerStats(newCharacter.getName());
            // call relevant services to initialise data
            inventoryService.createInventoryForNewCharacter(newCharacter.getName()).blockingGet();
//            attributeService.createBaseAttributes(newCharacter.getName());
            levelAttributeService.initializeCharacterClass(
                    newCharacter.getName(), createCharacterRequest.getClassName());
            // blocking call to ensure we do our rollback if anything happens.
            playerMotionService.initializePlayerMotion(newCharacter.getName()).blockingGet();
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
        statsService.deleteStatsFor(playerName);
    }

    // TODO: Support deletes
}
