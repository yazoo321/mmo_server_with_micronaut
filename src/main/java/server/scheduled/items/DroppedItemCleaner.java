package server.scheduled.items;

import io.micronaut.scheduling.annotation.Scheduled;
import server.items.repository.ItemRepository;

import jakarta.inject.Inject;

public class DroppedItemCleaner {

    @Inject
    ItemRepository itemRepository;

    @Scheduled(fixedDelay = "10s")
    void cleanItemsOnSchedule() {
        itemRepository.deleteTimedOutDroppedItems();
    }
}
