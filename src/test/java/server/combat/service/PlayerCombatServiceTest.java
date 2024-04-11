package server.combat.service;

import static org.awaitility.Awaitility.await;

import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.reactivestreams.Publisher;
import server.attribute.stats.model.Stats;
import server.attribute.stats.service.StatsService;
import server.attribute.status.service.StatusService;
import server.combat.model.CombatData;
import server.combat.model.CombatRequest;
import server.common.dto.Motion;
import server.items.equippable.model.EquippedItems;
import server.items.equippable.service.EquipItemService;
import server.items.helper.ItemTestHelper;
import server.items.model.Item;
import server.items.model.ItemInstance;
import server.items.types.ItemType;
import server.monster.server_integration.service.MobInstanceService;
import server.motion.dto.PlayerMotion;
import server.motion.service.PlayerMotionService;
import server.session.SessionParamHelper;
import server.socket.model.SocketResponseSubscriber;
import server.socket.session.FakeSession;

@MicronautTest
class PlayerCombatServiceTest {

    @Inject private StatsService statsService;

    @Inject private StatusService statusService;

    @Inject private SessionParamHelper sessionParamHelper;

    @Spy private FakeSession session;

    @Inject PlayerMotionService playerMotionService;

    @Inject MobInstanceService mobInstanceService;

    @Inject EquipItemService equipItemService;

    @Inject ItemTestHelper itemTestHelper;

    @Inject private PlayerCombatService playerCombatService;

    @Inject private SocketResponseSubscriber socketResponseSubscriber;

    @MockBean(SocketResponseSubscriber.class)
    public SocketResponseSubscriber socketResponseSubscriber() {
        return Mockito.mock(SocketResponseSubscriber.class);
    }

    @BeforeEach
    void setUp() {
        cleanup();
        MockitoAnnotations.openMocks(this);
        Publisher mockSubscriber = Mockito.mock(Publisher.class);
        Mockito.when(session.send(Mockito.any())).thenReturn(mockSubscriber);
    }

    @AfterEach
    void clearData() {
        cleanup();
    }

    private void cleanup() {
        itemTestHelper.deleteAllItemData();
    }

    private final String CHARACTER_1 = "character1";
    private final String MOB_1 = UUID.randomUUID().toString();

    @Test
    void testRequestAttackWithValidRequest() {
        // Given
        // prepare character
        Stats stats = statsService.initializePlayerStats(CHARACTER_1)
                .doOnError(err -> err.printStackTrace()).blockingGet();
        CombatData combatData = sessionParamHelper.getSharedActorCombatData(CHARACTER_1);
        sessionParamHelper.setSharedActorCombatData(combatData.getActorId(), combatData);
        PlayerMotion playerMotion =
                playerMotionService.initializePlayerMotion(CHARACTER_1).blockingGet();
        SessionParamHelper.setActorId(session, CHARACTER_1);
        sessionParamHelper.setMotion(session, playerMotion.getMotion(), CHARACTER_1);

        // create weapon and equip it
        equipWeapon(CHARACTER_1, session);

        // prepare mob
        Motion mobMotion = new Motion();
        mobMotion.setX(playerMotion.getMotion().getX() + 10);
        mobMotion.setY(playerMotion.getMotion().getY());
        mobMotion.setZ(playerMotion.getMotion().getZ());

        mobInstanceService.createMob(MOB_1, mobMotion).blockingGet();
        sessionParamHelper.setSharedActorMotion(MOB_1, mobMotion);

        // prepare combat request
        CombatRequest validCombatRequest = new CombatRequest();
        validCombatRequest.setTargets(new HashSet<>(List.of(MOB_1)));

        // When
        playerCombatService.requestAttack(session, validCombatRequest);

        // Then
        // check the attack loop has begun
        combatData =
                sessionParamHelper.getSharedActorCombatData(SessionParamHelper.getActorId(session));
        Set<String> targets = combatData.getTargets();
        Assertions.assertThat(targets.size()).isEqualTo(1);

        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(9, ChronoUnit.SECONDS))
                .until(
                        () -> {
                            try {
                                return statusService.getActorStatus(MOB_1).isDead();
//                                statsService.getStatsFor(MOB_1).blockingGet();
//                                return false;
                            } catch (NoSuchElementException e) {
                                // when publisher is empty, the mob was killed and deleted. later
                                // needs to be refactored to mob death state.
                                return true;
                            }
                        });
    }

    private EquippedItems equipWeapon(String actorId, WebSocketSession session) {
        itemTestHelper.prepareInventory(actorId);

        Item item = itemTestHelper.createAndInsertItem(ItemType.WEAPON.getType());
        ItemInstance itemInstance =
                itemTestHelper.createItemInstanceFor(item, UUID.randomUUID().toString());

        itemTestHelper.addItemToInventory(CHARACTER_1, itemInstance);

        EquippedItems equippedItem =
                equipItemService.equipItem(itemInstance.getItemInstanceId(), actorId).blockingGet();

        if (session != null) {
            sessionParamHelper.addToEquippedItems(session, equippedItem);
        }

        return equippedItem;
    }
}
