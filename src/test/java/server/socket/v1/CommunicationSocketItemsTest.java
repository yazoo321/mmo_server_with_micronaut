package server.socket.v1;

import static org.awaitility.Awaitility.await;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import server.common.dto.Location;
import server.items.inventory.model.response.GenericInventoryData;
import server.items.model.Item;
import server.items.model.ItemInstance;
import server.items.types.ItemType;
import server.socket.model.SocketMessage;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseType;
import server.socket.model.types.MessageType;
import server.socket.v1.base.CommunicationSocketTestBase;
import server.util.websocket.TestWebSocketClient;

// This test is designed to test the items flow
// dropping items, picking items up, etc.
@Property(name = "spec.name", value = "PlayerMotionSocketTest")
public class CommunicationSocketItemsTest extends CommunicationSocketTestBase {

    @Test
    void testWhenPlayerDropsItemItIsDropped() {
        playerMotionService.initializePlayerMotion(CHARACTER_1).blockingSubscribe();
        playerMotionService.initializePlayerMotion(CHARACTER_2).blockingSubscribe();

        TestWebSocketClient playerClient1 = createWebSocketClient(embeddedServer.getPort());
        TestWebSocketClient playerClient2 = createWebSocketClient(embeddedServer.getPort());

        initializeCharacters(playerClient1, playerClient2);

        ItemInstance createdItem = prepareCharactersAndItems();


        Location dropLocation = new Location(createBaseMotion());
        SocketMessage dropRequestChar1 =
                dropRequestForCharacter(CHARACTER_1, createdItem.getItemInstanceId(), dropLocation);

        playerClient1.send(dropRequestChar1);

        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
                .until(
                        () -> {
                            List<String> resTypes =
                                    getSocketResponse(playerClient1).stream()
                                            .map(SocketResponse::getMessageType)
                                            .toList();
                            return resTypes.contains(SocketResponseType.INVENTORY_UPDATE.getType())
                                    && resTypes.contains(
                                            SocketResponseType.ADD_ITEMS_TO_MAP.getType());
                        });

        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
                .until(
                        () -> {
                            List<String> resTypes =
                                    getSocketResponse(playerClient2).stream()
                                            .map(SocketResponse::getMessageType)
                                            .toList();
                            return resTypes.contains(SocketResponseType.ADD_ITEMS_TO_MAP.getType());
                        });
        List<SocketResponse> res = getSocketResponse(playerClient1);
        SocketResponse client1Inventory =
                getLastMessageOfType(res, SocketResponseType.INVENTORY_UPDATE.getType());
        Assertions.assertThat(
                        client1Inventory.getInventoryData().getInventory().getCharacterItems())
                .isEmpty();

        SocketResponse client1DroppedItem =
                getLastMessageOfType(res, SocketResponseType.ADD_ITEMS_TO_MAP.getType());
        SocketResponse client2DroppedItem =
                getLastMessageOfType(
                        getSocketResponse(playerClient2),
                        SocketResponseType.ADD_ITEMS_TO_MAP.getType());

        Assertions.assertThat(client1DroppedItem.getDroppedItems().size()).isEqualTo(1);
        Assertions.assertThat(client1DroppedItem.getDroppedItems())
                .usingRecursiveComparison()
                .isEqualTo(client2DroppedItem.getDroppedItems());

        List<String> instanceIds = client1DroppedItem.getDroppedItems().keySet().stream().toList();
        ItemInstance actualDroppedItemInstance =
                client1DroppedItem.getDroppedItems().get(instanceIds.get(0)).getItemInstance();

        // TODO: There seems to be a bug in serializing item.category within the test.
        Assertions.assertThat(actualDroppedItemInstance)
                .usingRecursiveComparison()
                .ignoringFields("item.category")
                .isEqualTo(createdItem);

        // now have character 2 pickup this item
        playerClient1.clearMessageHistory();
        playerClient2.clearMessageHistory();

        SocketMessage pickupMessage =
                pickupRequestForCharacter(
                        CHARACTER_2,
                        client1DroppedItem
                                .getDroppedItems()
                                .get(instanceIds.get(0))
                                .getItemInstanceId());

        playerClient2.send(pickupMessage);

        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
                .until(
                        () -> {
                            List<String> resTypes =
                                    getSocketResponse(playerClient2).stream()
                                            .map(SocketResponse::getMessageType)
                                            .toList();
                            return resTypes.contains(
                                            SocketResponseType.REMOVE_ITEMS_FROM_MAP.getType())
                                    && resTypes.contains(
                                            SocketResponseType.INVENTORY_UPDATE.getType());
                        });

        res = getSocketResponse(playerClient2);
        SocketResponse itemRemovedResponse =
                getLastMessageOfType(res, SocketResponseType.REMOVE_ITEMS_FROM_MAP.getType());

        Assertions.assertThat(itemRemovedResponse.getItemInstanceIds())
                .isEqualTo(Set.of(createdItem.getItemInstanceId()));

        SocketResponse inventoryUpdate =
                getLastMessageOfType(res, SocketResponseType.INVENTORY_UPDATE.getType());

        Assertions.assertThat(
                        inventoryUpdate
                                .getInventoryData()
                                .getInventory()
                                .getCharacterItems()
                                .get(0)
                                .getItemInstance()
                                .getItemInstanceId())
                .isEqualTo(createdItem.getItemInstanceId());
    }

    private void initializeCharacters(TestWebSocketClient client1, TestWebSocketClient client2) {
        SocketMessage char1WithinRange = createMessageForMotionWithinRange(CHARACTER_1);
        SocketMessage char2WithinRange = createMessageForMotionWithinRange(CHARACTER_2);

        client1.send(char1WithinRange);
        client2.send(char2WithinRange);

        SocketMessage init1 = createMessageForSessionParams(true, CHARACTER_1);
        SocketMessage init2 = createMessageForSessionParams(true, CHARACTER_2);

        client1.send(init1);
        client2.send(init2);
    }

    private SocketMessage dropRequestForCharacter(
            String actorId, String itemInstanceId, Location dropLocation) {
        SocketMessage message = new SocketMessage();
        message.setUpdateType(MessageType.DROP_ITEM.getType());
        GenericInventoryData genericInventoryData = new GenericInventoryData();
        genericInventoryData.setActorId(actorId);
        genericInventoryData.setItemInstanceId(itemInstanceId);
        genericInventoryData.setLocation(dropLocation);

        message.setInventoryRequest(genericInventoryData);

        return message;
    }

    private SocketMessage pickupRequestForCharacter(String actorId, String itemInstanceId) {
        SocketMessage message = new SocketMessage();
        message.setUpdateType(MessageType.PICKUP_ITEM.getType());
        GenericInventoryData inventoryData = new GenericInventoryData();
        inventoryData.setActorId(actorId);
        inventoryData.setItemInstanceId(itemInstanceId);

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
