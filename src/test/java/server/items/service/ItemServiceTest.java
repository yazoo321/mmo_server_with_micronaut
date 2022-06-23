package server.items.service;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.common.dto.Location;
import server.items.dropped.model.DroppedItem;
import server.items.helper.ItemTestHelper;
import server.items.model.exceptions.ItemException;
import server.items.weapons.Weapon;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.List;

@MicronautTest
public class ItemServiceTest {

    @Inject
    ItemService itemService;

    @Inject
    ItemTestHelper itemTestHelper;

    @BeforeEach
    void cleanDb() {
        itemTestHelper.deleteAllItemData();
    }

    @Test
    void testGetItemByIdReturnsItemAsExpected() {
        // Given
        Weapon weapon = itemTestHelper.createAndInsertWeaponItem();

        // When


        // Then

    }

    @Test
    void testDropItemWillWorkAsExpectedWhenItemExists() {
        // Given
        Weapon weapon = itemTestHelper.createAndInsertWeaponItem();
        Location locationToDrop = new Location("map", 1, 1, 1);

        DroppedItem expectedDroppedItem = ItemTestHelper.createDroppedItem(locationToDrop, weapon);

        // When
        DroppedItem actual = itemService.dropItem(weapon.getItemId(), locationToDrop);

        // Then
        Assertions.assertThat(actual).usingRecursiveComparison().ignoringFields("droppedItemId", "droppedAt")
                .isEqualTo(expectedDroppedItem);
    }

    @Test
    void getDroppedItemByIdFindsTheItemAndReturnsIt() {
        // Given
        Weapon weapon = itemTestHelper.createAndInsertWeaponItem();
        Location locationToDrop = new Location("map", 1, 1, 1);
        DroppedItem expectedDroppedItem = itemTestHelper.createAndInsertDroppedItem(locationToDrop, weapon);

        // When
        DroppedItem actual = itemService.getDroppedItemById(expectedDroppedItem.getDroppedItemId());

        // Then
        Assertions.assertThat(actual).usingRecursiveComparison().ignoringFields("droppedAt")
                .isEqualTo(expectedDroppedItem);
        // check droppedAt separately as we drop microseconds.
        Assertions.assertThat(actual.getDroppedAt()).isCloseTo(expectedDroppedItem.getDroppedAt(),
                Assertions.within(1, ChronoUnit.MILLIS));
    }

    @Test
    void getItemsInMapWillReturnResultsWhenInRange() {
        // Given
        Weapon weapon = itemTestHelper.createAndInsertWeaponItem();
        Location firstDroppedLocation = new Location("map", 1, 1, 1);
        Location secondDroppedLocation = new Location("map", 2, 2, 1);
        Location thirdDroppedLocation = new Location("map", 1500, 1500, 1); // out of range

        DroppedItem firstDrop = itemTestHelper.createAndInsertDroppedItem(firstDroppedLocation, weapon);
        DroppedItem secondDrop = itemTestHelper.createAndInsertDroppedItem(secondDroppedLocation, weapon);
        DroppedItem thirdDrop = itemTestHelper.createAndInsertDroppedItem(thirdDroppedLocation, weapon);

        Location searchRadius = new Location("map", 50, 50, 50);
        List<DroppedItem> expectedList = List.of(firstDrop, secondDrop);
        // don't expect third item to appear as its out of range

        // When
        List<DroppedItem> actual = itemService.getItemsInMap(searchRadius);

        Assertions.assertThat(actual).usingRecursiveComparison()
                .ignoringFields("droppedAt")
                .isEqualTo(expectedList);
    }

    @Test
    void deleteDroppedItemWillRemoveIt() {
        // Given
        Weapon weapon = itemTestHelper.createAndInsertWeaponItem();
        Location locationToDrop = new Location("map", 1, 1, 1);
        DroppedItem droppedItem = itemTestHelper.createAndInsertDroppedItem(locationToDrop, weapon);

        // When
        itemService.deleteDroppedItem(droppedItem.getDroppedItemId());

        // Then
        org.junit.jupiter.api.Assertions.assertThrows(
                ItemException.class, () -> itemService.getDroppedItemById(droppedItem.getDroppedItemId()));
    }

}
