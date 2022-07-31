package server.player.character.service;

import server.common.dto.Motion;
import server.player.attributes.levels.service.PlayerLevelAttributeService;
import server.player.attributes.service.PlayerAttributeService;
import server.player.character.dto.AccountCharactersResponse;
import server.player.character.dto.Character;
import server.player.character.dto.CreateCharacterRequest;
import server.player.character.equippable.service.EquipItemService;
import server.player.character.inventory.service.InventoryService;
import server.player.character.repository.PlayerCharacterRepository;
import server.player.motion.socket.v1.service.PlayerMotionService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.annotation.XmlInlineBinaryData;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Singleton
public class PlayerCharacterService {

    @Inject
    PlayerCharacterRepository playerCharacterRepository;

    @Inject
    InventoryService inventoryService;

    @Inject
    PlayerAttributeService attributeService;

    @Inject
    PlayerLevelAttributeService levelAttributeService;

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

        // call relevant services to initialise data
        inventoryService.createInventoryForNewCharacter(newCharacter.getName());
        attributeService.createBaseAttributes(newCharacter.getName());
        levelAttributeService.initializeCharacterClass(newCharacter.getName(), createCharacterRequest.getClassName());
        playerMotionService.initializePlayerMotion(newCharacter.getName());

        return newCharacter;
    }

    // TODO: Support deletes
}
