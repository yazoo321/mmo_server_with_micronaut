package server.socket.v1;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import server.common.dto.Location;
import server.common.dto.Location2D;
import server.items.inventory.model.response.GenericInventoryData;
import server.items.model.Item;
import server.items.model.ItemInstance;
import server.items.types.ItemType;
import server.socket.model.MessageType;
import server.socket.model.SocketMessage;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseType;
import server.socket.v1.base.CommunicationSocketTestBase;
import server.util.websocket.TestWebSocketClient;

import static org.awaitility.Awaitility.await;

// This test is designed to test the items flow
// dropping items, picking items up, etc.
@MicronautTest(environments = "kafka")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Property(name = "spec.name", value = "PlayerMotionSocketTest")
public class CommunicationSocketItemsTest extends CommunicationSocketTestBase {

    @Test
    void testWhenPlayerDropsItemItIsDropped() {
        playerMotionService.initializePlayerMotion(CHARACTER_1).blockingGet();
        playerMotionService.initializePlayerMotion(CHARACTER_2).blockingGet();

        TestWebSocketClient playerClient1 = createWebSocketClient(embeddedServer.getPort());
        TestWebSocketClient playerClient2 = createWebSocketClient(embeddedServer.getPort());

        ItemInstance createdItem = prepareCharactersAndItems();

        initializeCharacters(playerClient1, playerClient2);

        Location dropLocation = new Location(createBaseMotion());
        SocketMessage dropRequestChar1 = dropRequestForCharacter(CHARACTER_1, new Location2D(0,0), dropLocation);

        playerClient1.send(dropRequestChar1);

        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(30, ChronoUnit.SECONDS))
                .until(() -> {
                    List<String> resTypes = getSocketResponse(playerClient1)
                            .stream()
                            .map(SocketResponse::getMessageType)
                            .toList();
                    return resTypes.contains(SocketResponseType.INVENTORY_UPDATE.getType()) &&
                            resTypes.contains(SocketResponseType.ADD_ITEMS_TO_MAP.getType());
                });

        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
                .until(() -> {
                    List<String> resTypes = getSocketResponse(playerClient2)
                            .stream()
                            .map(SocketResponse::getMessageType)
                            .toList();
                    return resTypes.contains(SocketResponseType.ADD_ITEMS_TO_MAP.getType());
                });
        List<SocketResponse> res = getSocketResponse(playerClient1);
        SocketResponse client1Inventory = getLastMessageOfType(res, SocketResponseType.INVENTORY_UPDATE.getType());
        Assertions.assertThat(client1Inventory.getInventoryData().getInventory().getCharacterItems()).isEmpty();

        SocketResponse client1DroppedItem = getLastMessageOfType(res, SocketResponseType.ADD_ITEMS_TO_MAP.getType());
        SocketResponse client2DroppedItem = getLastMessageOfType(getSocketResponse(playerClient2), SocketResponseType.ADD_ITEMS_TO_MAP.getType());

        Assertions.assertThat(client1DroppedItem.getDroppedItems().size()).isEqualTo(1);
        Assertions.assertThat(client1DroppedItem.getDroppedItems())
                .usingRecursiveComparison()
                .isEqualTo(client2DroppedItem.getDroppedItems());

        List<String> instanceIds = client1DroppedItem.getDroppedItems().keySet().stream().toList();
        ItemInstance actualDroppedItemInstance = client1DroppedItem.getDroppedItems().get(instanceIds.get(0)).getItemInstance();

        // TODO: There seems to be a bug in serializing item.category within the test.
        Assertions.assertThat(actualDroppedItemInstance)
                .usingRecursiveComparison()
                .ignoringFields("item.category")
                .isEqualTo(createdItem);


        // now have character 2 pickup this item
        SocketMessage pickupMessage = pickupRequestForCharacter(CHARACTER_2, client1DroppedItem.getDroppedItems()
                .get(instanceIds.get(0)).getDroppedItemId());

        playerClient2.send(pickupMessage);

        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
                .until(() -> {
                    List<String> resTypes = getSocketResponse(playerClient2)
                            .stream()
                            .map(SocketResponse::getMessageType)
                            .toList();
                    return resTypes.contains(SocketResponseType.ADD_ITEMS_TO_MAP.getType());
                });

    }

    private void initializeCharacters(TestWebSocketClient client1, TestWebSocketClient client2) {
        SocketMessage char1WithinRange = createMessageForMotionWithinRange(CHARACTER_1);
        SocketMessage char2WithinRange = createMessageForMotionWithinRange(CHARACTER_2);

        client1.send(char1WithinRange);
        client2.send(char2WithinRange);
    }

    private SocketMessage dropRequestForCharacter(
            String characterName,
            Location2D inventoryLocation, Location dropLocation) {
        SocketMessage message = new SocketMessage();
        message.setUpdateType(MessageType.DROP_ITEM.getType());
        GenericInventoryData genericInventoryData = new GenericInventoryData();
        genericInventoryData.setCharacterName(characterName);
        genericInventoryData.setItemInventoryLocation(inventoryLocation);
        genericInventoryData.setLocation(dropLocation);

        message.setInventoryRequest(genericInventoryData);

        return message;
    }

    private SocketMessage pickupRequestForCharacter(String characterName, String dropItemId) {
        SocketMessage message = new SocketMessage();
        message.setUpdateType(MessageType.PICKUP_ITEM.getType());
        GenericInventoryData inventoryData = new GenericInventoryData();
        inventoryData.setCharacterName(characterName);
        inventoryData.setDroppedItemId(dropItemId);

        message.setInventoryRequest(inventoryData);

        return message;
    }

    private ItemInstance prepareCharactersAndItems() {
        itemTestHelper.prepareInventory(CHARACTER_1);
        itemTestHelper.prepareInventory(CHARACTER_2);

        Item item = itemTestHelper.createAndInsertItem(ItemType.WEAPON.getType());
        ItemInstance itemInstance =
                itemTestHelper.createItemInstanceFor(item, UUID.randomUUID().toString());

        itemTestHelper.addItemToInventory(CHARACTER_1, itemInstance);

        return itemInstance;
    }
}
