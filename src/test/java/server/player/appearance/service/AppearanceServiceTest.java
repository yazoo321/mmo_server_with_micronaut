package server.player.appearance.service;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import server.player.appearance.AppearanceTestHelper;
import server.player.appearance.model.AppearancePiece;
import server.player.appearance.model.CreateCharacterGenericReq;
import server.player.appearance.utils.Race;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AppearanceServiceTest {

    @Inject
    AppearanceService appearanceService;

    @Inject
    AppearanceTestHelper appearanceTestHelper;

    @BeforeEach
    void prepare() {
       appearanceTestHelper.clearAllAppearanceData();
    }

    @AfterAll()
    void deleteAllData() {
        appearanceTestHelper.clearAllAppearanceData();
    }

    @Test
    void insertingNewItemPieceWillMakeItAvailable() {
        // Given
        List<AppearancePiece> pieces = new ArrayList<>();
        pieces.add(AppearanceTestHelper.createBasicMaleHeadPiece());
        pieces.add(AppearanceTestHelper.createBasicMaleHairPiece());
        pieces.add(AppearanceTestHelper.createBasicMaleChestPiece());
        pieces.add(AppearanceTestHelper.createBasicFemaleHeadPiece());

        // When
        appearanceService.insertAppearancePieces(pieces);

        // Then
        List<AppearancePiece> actual = appearanceTestHelper.getAllBasePieces();
        Assertions.assertThat(actual).usingRecursiveComparison().isEqualTo(pieces);
    }

    @Test
    void gettingDefaultAppearancePiecesWillFetchDesiredPieces() {
        // Given
        List<AppearancePiece> pieces = new ArrayList<>();
        pieces.add(AppearanceTestHelper.createBasicMaleHeadPiece());
        pieces.add(AppearanceTestHelper.createBasicMaleHairPiece());
        pieces.add(AppearanceTestHelper.createBasicMaleChestPiece());
        pieces.add(AppearanceTestHelper.createBasicFemaleHeadPiece());
        appearanceService.insertAppearancePieces(pieces);

        // When
        appearanceTestHelper.getAllBasePieces();

        CreateCharacterGenericReq res = appearanceService.getDefaultAppearance(
                Race.HUMAN.value, true);

        List<AppearancePiece> actual = new ArrayList<>();

        res.getCharacterAppearance().getProperties().forEach((key, piece) -> actual.add(piece));

        // Then
        List<AppearancePiece> expected = pieces.stream().filter(AppearancePiece::getIsMale).collect(Collectors.toList());
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expected);

    }

    @Test
    void gettingAllHairStylesWillReturnExpectedResults() {
        // Given
        AppearancePiece hair1 = AppearanceTestHelper.createBasicMaleHairPiece();
        AppearancePiece hair2 = AppearanceTestHelper.createBasicMaleHairPiece();
        AppearancePiece hair3 = AppearanceTestHelper.createBasicMaleHairPiece();
        AppearancePiece hair4 = AppearanceTestHelper.createBasicMaleHairPiece();

        hair2.setMesh("HAIR_MESH_2");
        hair3.setMaterial("HAIR_MATERIAL_2"); // this should not return as the mesh is the same but different color
        hair4.setIsMale(false); // this should not return as we're searching male style

        List<String> expected = Stream.of(hair1, hair2).map(AppearancePiece::getMesh)
                .collect(Collectors.toList());

        appearanceService.insertAppearancePieces(List.of(hair1, hair2, hair3, hair4));

        // When
        List<String> actual = appearanceService.getAllHairOptions(Race.HUMAN.value, true);

        // Then
        Assertions.assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void gettingAllHairColorsWillReturnExpectedResults() {
        // Given
        AppearancePiece hair1 = AppearanceTestHelper.createBasicMaleHairPiece();
        AppearancePiece hair2 = AppearanceTestHelper.createBasicMaleHairPiece();
        AppearancePiece hair3 = AppearanceTestHelper.createBasicMaleHairPiece();
        AppearancePiece hair4 = AppearanceTestHelper.createBasicMaleHairPiece();

        hair2.setMaterial("MATERIAL_2");
        hair3.setMesh("Mesh2");     // ignored because material will be same as 1
        hair4.setIsMale(false);     // ignored because we're searching male hair colors

        List<String> expected = Stream.of(hair1, hair2).map(AppearancePiece::getMaterial)
                .collect(Collectors.toList());

        appearanceService.insertAppearancePieces(List.of(hair1, hair2, hair3, hair4));

        // When
        List<String> actual = appearanceService.getAllHairColors(Race.HUMAN.value, true);

        // Then
        Assertions.assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void gettingAllSkinColorsWillReturnExpectedResults() {
        // Given
        AppearancePiece head1 = AppearanceTestHelper.createBasicMaleHeadPiece();
        AppearancePiece head2 = AppearanceTestHelper.createBasicMaleHeadPiece();
        AppearancePiece head3 = AppearanceTestHelper.createBasicMaleHeadPiece();
        AppearancePiece head4 = AppearanceTestHelper.createBasicMaleHeadPiece();

        head2.setMaterial("MATERIAL_2");
        head3.setMesh("MESH_2");    // ignored because skin color is based off material
        head4.setIsMale(false);     // will be ignored as filter includes gender, should have another entry if we want to explicitly include

        List<String> expected = Stream.of(head1, head2).map(AppearancePiece::getMaterial)
                .collect(Collectors.toList());

        appearanceService.insertAppearancePieces(List.of(head1, head2, head3, head4));

        // When
        List<String> actual = appearanceService.getAllSkinColours(Race.HUMAN.value, true);

        // Then
        Assertions.assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }
}
