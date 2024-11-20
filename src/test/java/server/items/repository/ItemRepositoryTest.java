package server.items.repository;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.attribute.stats.types.StatsTypes;
import server.items.helper.ItemTestHelper;
import server.items.model.Item;
import server.items.model.Stacking;
import server.items.types.ItemType;
import server.items.types.weapons.Weapon;

import java.util.List;
import java.util.Map;

@MicronautTest
public class ItemRepositoryTest {

    @Inject ItemRepository itemRepository;

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
        Item found = itemRepository.findByItemId(weapon.getItemId()).blockingGet();

        // Then
        Assertions.assertThat(found).usingRecursiveComparison().isEqualTo(weapon);
    }

    @Test
    void testFindItemsForLevels() {
        Item weapon = ItemTestHelper.createTestItemOfType("WEAPON");
        Item helm = ItemTestHelper.createTestItemOfType("HELM");
        helm.setRequirements(Map.of(
                StatsTypes.STR.getType(), 20,
                StatsTypes.INT.getType(), 20,
                StatsTypes.DEX.getType(), 15,
                StatsTypes.STA.getType(), 15
        ));

        List<Item> testItems = List.of(
                weapon, helm
        );

        insertTestItems(testItems);

        List<Item> items = itemRepository.findItemsForLevels(1, 2).blockingGet();

        Assertions.assertThat(items)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(List.of(
                        testItems.get(0)
                ));

        items = itemRepository.findItemsForLevels(3, 4).blockingGet();

        Assertions.assertThat(items)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(List.of(
                        testItems.get(1)
                ));

    }

    // Utility function for inserting test data into the database
    private void insertTestItems(List<Item> items) {
        // Code to insert items into the MongoDB collection
        items.forEach(item -> itemRepository.upsertItem(item).subscribe());
    }
}
