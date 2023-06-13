package server.items.repository;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.items.helper.ItemTestHelper;
import server.items.model.Item;
import server.items.types.ItemType;
import server.items.types.weapons.Weapon;

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
}
