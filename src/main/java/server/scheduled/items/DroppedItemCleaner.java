package server.scheduled.items;

import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import server.items.repository.ItemRepository;

public class DroppedItemCleaner {

    @Inject ItemRepository itemRepository;

    @Scheduled(fixedDelay = "10s")
    void cleanItemsOnSchedule() {
        itemRepository.deleteTimedOutDroppedItems();
    }
}
