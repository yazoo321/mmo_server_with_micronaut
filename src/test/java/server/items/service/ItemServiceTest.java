package server.items.service;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.common.dto.Location;
import server.items.helper.ItemTestHelper;
import server.items.model.DroppedItem;
import server.items.model.ItemInstance;
import server.items.types.ItemType;
import server.items.types.weapons.Weapon;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

@MicronautTest
public class ItemServiceTest {

    @Inject ItemService itemService;

    @Inject ItemTestHelper itemTestHelper;

    @BeforeEach
    void cleanDb() {
        itemTestHelper.deleteAllItemData();
    }

    @Test
    void testGetItemByIdReturnsItemAsExpected() {
        // Given
        Weapon weapon = (Weapon) itemTestHelper.createAndInsertItem(ItemType.WEAPON.getType());

        // When

        // Then

    }

    @Test
    void testDropItemWillWorkAsExpectedWhenItemExists() {
        // Given
        Weapon weapon = (Weapon) itemTestHelper.createAndInsertItem(ItemType.WEAPON.getType());
        Location locationToDrop = new Location("map", 1, 1, 1);

        DroppedItem expectedDroppedItem =
                new DroppedItem(
                        "itemInstanceId",
                        locationToDrop,
                        new ItemInstance(weapon.getItemId(), "itemInstanceId", weapon),
                        LocalDateTime.now());

        // When
        DroppedItem actual =
                itemService.createNewDroppedItem(weapon.getItemId(), locationToDrop).blockingGet();

        // Then
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("itemInstanceId", "droppedAt", "itemInstance.itemInstanceId")
                .isEqualTo(expectedDroppedItem);
    }

    @Test
    void getDroppedItemByIdFindsTheItemAndReturnsIt() {
        // Given
        Weapon weapon = (Weapon) itemTestHelper.createAndInsertItem(ItemType.WEAPON.getType());
        Location locationToDrop = new Location("map", 1, 1, 1);
        DroppedItem expectedDroppedItem =
                itemTestHelper.createAndInsertDroppedItem(locationToDrop, weapon);

        // When
        DroppedItem actual =
                itemService
                        .getDroppedItemByInstanceId(expectedDroppedItem.getItemInstanceId())
                        .blockingGet();

        // Then
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("droppedAt")
                .isEqualTo(expectedDroppedItem);
        // check droppedAt separately as we drop microseconds.
        Assertions.assertThat(actual.getDroppedAt())
                .isCloseTo(
                        expectedDroppedItem.getDroppedAt(),
                        Assertions.within(1, ChronoUnit.MILLIS));
    }

    @Test
    void getItemsInMapWillReturnResultsWhenInRange() {
        // Given
        Weapon weapon = (Weapon) itemTestHelper.createAndInsertItem(ItemType.WEAPON.getType());
        Location firstDroppedLocation = new Location("map", 1, 1, 1);
        Location secondDroppedLocation = new Location("map", 2, 2, 1);
        Location thirdDroppedLocation = new Location("map", 1500, 1500, 1); // out of range

        DroppedItem firstDrop =
                itemTestHelper.createAndInsertDroppedItem(firstDroppedLocation, weapon);
        DroppedItem secondDrop =
                itemTestHelper.createAndInsertDroppedItem(secondDroppedLocation, weapon);
        DroppedItem thirdDrop =
                itemTestHelper.createAndInsertDroppedItem(thirdDroppedLocation, weapon);

        Location searchRadius = new Location("map", 50, 50, 50);
        List<DroppedItem> expectedList = List.of(firstDrop, secondDrop);
        // don't expect third item to appear as its out of range

        // When
        List<DroppedItem> actual = itemService.getItemsInMap(searchRadius).blockingGet();

        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("droppedAt")
                .isEqualTo(expectedList);
    }

    @Test
    void deleteDroppedItemWillRemoveIt() {
        // Given
        Weapon weapon = (Weapon) itemTestHelper.createAndInsertItem(ItemType.WEAPON.getType());
        Location locationToDrop = new Location("map", 1, 1, 1);
        DroppedItem droppedItem = itemTestHelper.createAndInsertDroppedItem(locationToDrop, weapon);

        // When
        itemService.deleteDroppedItem(droppedItem.getItemInstanceId()).blockingGet();

        // Then
        // TODO: Check for ItemException nested
        org.junit.jupiter.api.Assertions.assertThrows(
                NoSuchElementException.class,
                () ->
                        itemService
                                .getDroppedItemByInstanceId(droppedItem.getItemInstanceId())
                                .blockingGet());
    }
}
