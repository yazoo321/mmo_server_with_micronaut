package server.monster.server_integration.service;

import jakarta.inject.Inject;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.items.dropped.model.DroppedItem;
import server.items.service.ItemService;
import server.monster.server_integration.model.MobUpdate;

@Service
@Slf4j
public class MonsterDeathService {

    // this service will be responsible for followup after monster has been killed
    // This service is in progress / pre-mvp

    @Inject ItemService itemService;

    Random rd = new Random();

    List<String> validItemIds = List.of("500", "600", "700", "800");

    public void handleMonsterDeath(MobUpdate mobUpdate) {
        // drop item near mob location
        String itemIdToDrop = getItemIdToDrop();

        Motion motion = mobUpdate.getMotion();
        Location location = new Location(motion);
        applyRandomnessToLocation(location);

        DroppedItem droppedItem = itemService.createNewDroppedItem(itemIdToDrop, location);
        log.info("Dropped item: {}", droppedItem);
    }

    private String getItemIdToDrop() {
        // this function will be part of a separate service, this is mvp.

        return validItemIds.get(rd.nextInt(3));
    }

    private void applyRandomnessToLocation(Location location) {
        int margin = 40;
        location.setX(location.getX() + rd.nextInt(margin) - (margin / 2));
        location.setY(location.getY() + rd.nextInt(margin) - (margin / 2));
        location.setX(location.getZ() + 50); // always make item vertically higher, let it 'drop'
    }
}
