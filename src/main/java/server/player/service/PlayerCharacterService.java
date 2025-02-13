package server.player.service;

import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import server.actionbar.service.ActionbarService;
import server.attribute.stats.service.PlayerLevelStatsService;
import server.attribute.stats.service.StatsService;
import server.attribute.status.service.StatusService;
import server.attribute.talents.service.TalentService;
import server.items.equippable.service.EquipItemService;
import server.items.inventory.service.InventoryService;
import server.motion.service.PlayerMotionService;
import server.player.model.AccountCharactersResponse;
import server.player.model.Character;
import server.player.model.CreateCharacterRequest;
import server.player.repository.PlayerCharacterRepository;
import server.skills.service.CombatSkillsService;

@Slf4j
@Singleton
public class PlayerCharacterService {

    @Inject PlayerCharacterRepository playerCharacterRepository;

    @Inject InventoryService inventoryService;

    @Inject PlayerLevelStatsService levelAttributeService;

    @Inject PlayerMotionService playerMotionService;

    @Inject StatsService statsService;

    @Inject StatusService statusService;

    @Inject TalentService talentService;

    @Inject CombatSkillsService combatSkillsService;

    @Inject ActionbarService actionbarService;

    @Inject EquipItemService equipItemService;

    public AccountCharactersResponse getAccountCharacters(String username) {
        List<Character> characterList = playerCharacterRepository.findByAccount(username);

        return new AccountCharactersResponse(characterList);
    }

    public AccountCharactersResponse getCharacters(List<String> names) {
        List<Character> characterList = playerCharacterRepository.findByName(names);

        return new AccountCharactersResponse(characterList);
    }

    public Single<List<Character>> getCharactersByNames(Set<String> actorIds) {
        return playerCharacterRepository.findByNames(actorIds);
    }

    public Character createCharacter(
            CreateCharacterRequest createCharacterRequest, String username) {
        log.info("Creating character: {}", createCharacterRequest.getName());
        createCharacterRequest.setClassName(createCharacterRequest.getClassName().toUpperCase());

        Character newCharacter = new Character();
        newCharacter.setName(createCharacterRequest.getName());
        newCharacter.setAccountName(username);
        newCharacter.setAppearanceInfo(createCharacterRequest.getAppearanceInfo());
        newCharacter.setUpdatedAt(Instant.now());
        newCharacter.setIsOnline(false);

        // this will throw if there's a duplicate entry
        newCharacter = playerCharacterRepository.createNew(newCharacter);

        try {
            statsService.initializePlayerStats(newCharacter.getName()).blockingSubscribe();
            statusService.initializeStatus(newCharacter.getName()).blockingSubscribe();
            talentService.initializeActorTalents(newCharacter.getName()).blockingSubscribe();
            combatSkillsService.initialiseSkills(newCharacter.getName()).blockingSubscribe();
            log.info("status for actor initialized: {}", createCharacterRequest.getName());
            // call relevant services to initialise data
            inventoryService
                    .createInventoryForNewCharacter(newCharacter.getName())
                    .blockingSubscribe();
            //            attributeService.createBaseAttributes(newCharacter.getName());
            levelAttributeService
                    .initializeCharacterClass(
                            newCharacter.getName(), createCharacterRequest.getClassName())
                    .blockingSubscribe();
            // blocking call to ensure we do our rollback if anything happens.
            playerMotionService.initializePlayerMotion(newCharacter.getName()).blockingSubscribe();

        } catch (Exception e) {
            log.warn("Create character failed, rolling back changes");
            // we need to rollback the changes
            rollbackChanges(newCharacter.getName());

            // give the 'useful' message back
            throw e;
        }

        return newCharacter;
    }

    public void deleteCharacter(String actorId) {
        // TODO: Validate request

        rollbackChanges(actorId);
    }

    private void rollbackChanges(String actorId) {
        log.warn("Deleting character {}", actorId);
        inventoryService.clearAllDataForCharacter(actorId).blockingSubscribe();
        playerMotionService.deletePlayerMotion(actorId).blockingSubscribe();
        playerCharacterRepository.deleteByActorId(actorId);
        statsService.deleteStatsFor(actorId).blockingSubscribe();
        log.info("Deleting actor statuses: {}", actorId);
        statusService.deleteActorStatus(actorId).blockingSubscribe();
        log.info("actor statuses deleted");
        actionbarService.deleteActorActionbar(actorId).blockingSubscribe();
        equipItemService.deleteCharacterEquippedItems(actorId).blockingSubscribe();
        talentService.deleteActorTalents(actorId).blockingSubscribe();
    }

    // TODO: Support deletes
}
