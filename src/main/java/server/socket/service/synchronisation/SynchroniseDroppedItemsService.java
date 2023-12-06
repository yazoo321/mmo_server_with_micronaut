package server.socket.service.synchronisation;

import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.items.model.DroppedItem;
import server.items.service.ItemService;
import server.motion.model.SessionParams;
import server.session.SessionParamHelper;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseSubscriber;
import server.socket.model.SocketResponseType;

@Slf4j
@Singleton
public class SynchroniseDroppedItemsService {

    @Inject ItemService itemService;

    @Inject SocketResponseSubscriber socketResponseSubscriber;

    public void handleSynchroniseDroppedItems(Motion motion, WebSocketSession session) {
        String actorId =
                (String) session.asMap().getOrDefault(SessionParams.ACTOR_ID.getType(), "");
        if (SessionParamHelper.getIsServer(session)) {
            // don't synchronise dropped items on server instances
            return;
        }

        itemService
                .getItemsInMap(new Location(motion))
                .doOnError(e -> log.error("Failed to get items in map, {}", e.getMessage()))
                .doOnSuccess(
                        droppedItems -> {
                            Map<String, DroppedItem> droppedItemsMap =
                                    droppedItems.stream()
                                            .collect(
                                                    Collectors.toMap(
                                                            (i) ->
                                                                    i.getItemInstance()
                                                                            .getItemInstanceId(),
                                                            Function.identity()));

                            Set<String> currentItems = droppedItemsMap.keySet();
                            Set<String> trackedItems = getTrackedItems(session);

                            handleNewItems(currentItems, trackedItems, droppedItemsMap, session);
                            handleLostItems(currentItems, trackedItems, session);

                            session.asMap()
                                    .put(SessionParams.DROPPED_ITEMS.getType(), currentItems);
                        })
                .subscribe();
    }

    private Set<String> getTrackedItems(WebSocketSession session) {
        return (Set<String>)
                session.asMap()
                        .getOrDefault(SessionParams.DROPPED_ITEMS.getType(), new HashSet<>());
    }

    private Set<String> getLostItemIds(Set<String> currentItems, Set<String> trackedItems) {
        return trackedItems.stream()
                .filter(i -> !currentItems.contains(i))
                .collect(Collectors.toSet());
    }

    private Set<String> getNewItemIds(Set<String> currentItems, Set<String> trackedItems) {
        return currentItems.stream()
                .filter(i -> !trackedItems.contains(i))
                .collect(Collectors.toSet());
    }

    private void handleNewItems(
            Set<String> currentItems,
            Set<String> trackedItems,
            Map<String, DroppedItem> droppedItemsMap,
            WebSocketSession session) {
        Set<String> newItemIds = getNewItemIds(currentItems, trackedItems);

        if (newItemIds.isEmpty()) {
            return;
        }

        trackedItems.addAll(newItemIds);
        session.put(SessionParams.DROPPED_ITEMS.getType(), trackedItems);

        Map<String, DroppedItem> newItemsMap = new HashMap<>();
        newItemIds.forEach(id -> newItemsMap.put(id, droppedItemsMap.get(id)));

        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.ADD_ITEMS_TO_MAP.getType())
                        .droppedItems(newItemsMap)
                        .build();

        session.send(socketResponse).subscribe(socketResponseSubscriber);
    }

    private void handleLostItems(
            Set<String> currentItems, Set<String> trackedItems, WebSocketSession session) {
        Set<String> lostItemIds = getLostItemIds(currentItems, trackedItems);

        if (lostItemIds.isEmpty()) {
            return;
        }

        trackedItems.removeAll(lostItemIds);

        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.REMOVE_ITEMS_FROM_MAP.getType())
                        .itemInstanceIds(lostItemIds)
                        .build();

        session.send(socketResponse).subscribe(socketResponseSubscriber);
    }
}
