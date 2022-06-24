package server.items.repository;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.common.dto.Location;
import server.items.dropped.model.DroppedItem;
import server.items.helper.ItemTestHelper;
import server.items.model.Item;
import server.items.model.exceptions.ItemException;
import server.items.service.ItemService;
import server.items.types.ItemType;
import server.items.weapons.Weapon;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.List;

@MicronautTest
public class ItemRepositoryTest {

    @Inject
    ItemRepository itemRepository;

    @Inject
    ItemTestHelper itemTestHelper;

    @BeforeEach
    void cleanDb() {
        itemTestHelper.deleteAllItemData();
    }

    @Test
    void testGetItemByIdReturnsItemAsExpected() {
        // Given
        Weapon weapon = (Weapon) itemTestHelper.createAndInsertItem(ItemType.WEAPON.getType());

        // When
        Item found = itemRepository.findByItemId(weapon.getItemId());

        // Then
        Assertions.assertThat(found).usingRecursiveComparison().isEqualTo(weapon);
    }

}
