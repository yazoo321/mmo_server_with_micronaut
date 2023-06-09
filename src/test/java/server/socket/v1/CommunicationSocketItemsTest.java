package server.socket.v1;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.util.*;
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
import server.socket.v1.base.CommunicationSocketTestBase;
import server.util.websocket.TestWebSocketClient;

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

        prepareCharactersAndItems();
    }

    private SocketMessage dropRequestForCharacter(
            Location2D inventoryLocation, Location dropLocation) {
        SocketMessage message = new SocketMessage();
        message.setUpdateType(MessageType.DROP_ITEM.getType());
        GenericInventoryData genericInventoryData = new GenericInventoryData();
        genericInventoryData.setItemInventoryLocation(inventoryLocation);
        genericInventoryData.setLocation(dropLocation);

        message.setInventoryRequest(genericInventoryData);

        return message;
    }

    private void prepareCharactersAndItems() {
        itemTestHelper.prepareInventory(CHARACTER_1);
        itemTestHelper.prepareInventory(CHARACTER_2);

        Item item = itemTestHelper.createAndInsertItem(ItemType.WEAPON.getType());
        ItemInstance itemInstance =
                itemTestHelper.createItemInstanceFor(item, UUID.randomUUID().toString());

        itemTestHelper.addItemToInventory(CHARACTER_1, itemInstance);
    }
}
