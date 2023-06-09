package server.items.server_integration.listener;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@KafkaListener(
        groupId = "mmo-server",
        offsetReset = OffsetReset.EARLIEST,
        offsetStrategy = OffsetStrategy.SYNC,
        clientId = "item_processor_client")
public class ItemServerListener {
    // process and route any item related query

    // drop item, pickup item

    //    @Inject
    //    InventoryService inventoryService;
    //
    //    @Topic("drop-item")
    //    public void processDropItemRequest(GenericInventoryData request) {
    //        inventoryService.dropItemAsync(request);
    //    }
    //
    //    @Topic("pickup-item")
    //    public void processPickupItemRequest(GenericInventoryData request) {
    //        inventoryService.pickupItemAsync(request);
    //    }

}
