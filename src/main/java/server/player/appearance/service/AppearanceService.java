package server.player.appearance.service;

import lombok.extern.slf4j.Slf4j;
import server.items.service.ItemService;
import server.player.appearance.model.AppearancePiece;
import server.player.appearance.model.CharacterAppearance;
import server.player.appearance.model.CreateCharacterGenericReq;
import server.player.appearance.repository.AppearanceRepository;
import server.player.appearance.utils.Part;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class AppearanceService {

    @Inject
    AppearanceRepository appearanceRepository;

    @Inject
    ItemService itemService;

    // Get default appearance; for example, human male, human female, etc.
    public CreateCharacterGenericReq getDefaultAppearance(String race, Boolean isMale) {
        List<AppearancePiece> pieces =
                appearanceRepository.getDefaultAppearanceFor(race, isMale);

        Map<String, AppearancePiece> pieceMap =
                pieces.stream().collect(Collectors.toMap(AppearancePiece::getPart, Function.identity()));

        CharacterAppearance characterAppearance = new CharacterAppearance();
        characterAppearance.setRace(race);
        characterAppearance.setIsMale(isMale);
        // this will help us dynamically allocate skin color instead of keeping static reference

        characterAppearance.setProperties(pieceMap);

        CreateCharacterGenericReq responseObj = new CreateCharacterGenericReq();
        responseObj.setCharacterAppearance(characterAppearance);

        responseObj.setHairColorOptions(getAllHairOptions(race, isMale));
        responseObj.setHairStyleOptions(getAllHairOptions(race, isMale));
        responseObj.setSkinColorOptions(getAllSkinColours(race, isMale));

        // set default skin color to first option available
        characterAppearance.setSkinColor(responseObj.getSkinColorOptions().get(0));

        return responseObj;
    }

    public List<String> getAllSkinColours(String race, Boolean isMale) {
        // Change this as per your requirement.
        List<AppearancePiece> pieces = appearanceRepository.getBaseAppearanceUsing(race, isMale, Part.HEAD.value);

        return pieces.stream().map(AppearancePiece::getMaterial).distinct().collect(Collectors.toList());
    }

    public List<String> getAllHairColors(String race, Boolean isMale) {
        List<AppearancePiece> pieces = appearanceRepository.getBaseAppearanceUsing(race, isMale, Part.HAIR.value);

        return pieces.stream().map(AppearancePiece::getMaterial).distinct().collect(Collectors.toList());
    }

    public List<String> getAllHairOptions(String race, Boolean isMale) {
        List<AppearancePiece> pieces = appearanceRepository.getBaseAppearanceUsing(race, isMale, Part.HAIR.value);

        return pieces.stream().map(AppearancePiece::getMesh).distinct().collect(Collectors.toList());
    }

    public List<AppearancePiece> insertAppearancePieces(List<AppearancePiece> appearancePieceList) {
        // assign fresh uuid
        List<AppearancePiece> insertedPieces = new ArrayList<>();

        for (AppearancePiece piece : appearancePieceList) {
            // check if it exists

            AppearancePiece found =
                    appearanceRepository.findByPartMeshMaterial(piece.getPart(), piece.getMesh(), piece.getMaterial());

            if (found != null) {
                log.warn("appearance piece with {} {} {} already exists with id {}",
                        piece.getPart(), piece.getMesh(), piece.getMaterial(), found.getId());
                continue;
            }

            piece.setId(UUID.randomUUID().toString());
            String itemId = piece.getItemId();

            itemService.assignAppearanceIdToItem(itemId, piece.getId());

            appearanceRepository.savePiece(piece);
            insertedPieces.add(piece);
        }

        return insertedPieces;
    }

}
