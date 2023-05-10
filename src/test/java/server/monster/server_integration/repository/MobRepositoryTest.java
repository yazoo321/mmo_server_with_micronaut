package server.monster.server_integration.repository;

import static org.assertj.core.api.Assertions.*;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.monster.server_integration.model.Monster;

@MicronautTest
public class MobRepositoryTest {

    @Inject MobRepository mobRepository;

    private static String TEST_MAP = "TEST_MAP";

    private static final String mobInstanceId1 = "5068a37d-46bc-40f2-9179-e9689bbfa52b";
    private static final String mobInstanceId2 = "5068a37d-46bc-40f2-9179-e9689bbfa52c";
    private static final String mobInstanceId3 = "5068a37d-46bc-40f2-9179-e9689bbfa52d";

    @BeforeEach
    void deleteTestData() {
        mobRepository.deleteMobInstance(mobInstanceId1).blockingGet();
        mobRepository.deleteMobInstance(mobInstanceId2).blockingGet();
        mobRepository.deleteMobInstance(mobInstanceId3).blockingGet();
    }

    @Test
    void testFindMobMotion() {
        // Create a MobMotion object and insert it into the repository
        Monster mobMotion = createMobInstance(mobInstanceId1, 1000, 1000, 1000);
        mobRepository.insertMobInstance(mobMotion).blockingGet();

        // Retrieve the MobMotion by its instance ID
        Monster result = mobRepository.findMobInstance(mobMotion.getMobInstanceId()).blockingGet();

        // Verify that the retrieved MobMotion matches the inserted one
        assertThat(result).usingRecursiveComparison().isEqualTo(mobMotion);
    }

    @Test
    void testInsertMobMotion() {
        // Create a MobMotion object and insert it into the repository
        Monster mobMotion = createMobInstance(mobInstanceId1, 1000, 1000, 1000);
        mobRepository.insertMobInstance(mobMotion).blockingGet();

        // Retrieve the MobMotion by its instance ID
        Single<Monster> result = mobRepository.findMobInstance(mobMotion.getMobInstanceId());

        // Verify that the retrieved MobMotion matches the inserted one
        assertThat(result.blockingGet()).usingRecursiveComparison().isEqualTo(mobMotion);
    }

//    @Test
//    void testUpdateMobMotion() {
//        // Create a MobMotion object and insert it into the repository
//        Monster mobMotion = createMobInstance(mobInstanceId1, 1000, 1000, 1000);
//        mobRepository.insertMobInstance(mobMotion).blockingGet();
//
//        // Update the motion of the MobMotion object
//        mobMotion.getMotion().setX(42);
//        mobMotion.getMotion().setY(84);
//        mobRepository.updateMobMotion(mobMotion).blockingGet();
//
//        // Retrieve the updated MobMotion by its instance ID
//        Monster result = mobRepository.findMobInstance(mobMotion.getMobInstanceId()).blockingGet();
//
//        // Verify that the retrieved MobMotion matches the updated one
//        assertThat(result).usingRecursiveComparison().isEqualTo(mobMotion);
//    }

    @Test
    void testUpdateMotionOnly() {
        // Create a MobMotion object and insert it into the repository
        Monster mob = createMobInstance(mobInstanceId1, 1000, 1000, 1000);
        mobRepository.insertMobInstance(mob).blockingGet();

        // Update the motion of the MobMotion object
        mob.getMotion().setX(42);
        mob.getMotion().setY(84);
        mobRepository.updateMotionOnly(mob.getMobInstanceId(), mob.getMotion()).blockingGet();

        // Retrieve the updated MobMotion by its instance ID
        Monster result = mobRepository.findMobInstance(mob.getMobInstanceId()).blockingGet();

        // Verify that the retrieved MobMotion matches the updated one
        assertThat(result).usingRecursiveComparison().ignoringFields("updatedAt").isEqualTo(mob);
        assertThat(result.getUpdatedAt())
                .isCloseTo(mob.getUpdatedAt(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    void testDeleteMobInstance() {
        // Create a MobMotion object and insert it into the repository
        Monster mobMotion = createMobInstance(mobInstanceId1, 1000, 1000, 1000);
        mobRepository.insertMobInstance(mobMotion).blockingGet();

        // Delete the MobMotion object by its instance ID
        mobRepository.deleteMobInstance(mobMotion.getMobInstanceId()).blockingGet();

        // Attempt to retrieve the deleted MobMotion by its instance ID
        // Verify that the retrieved result is null
        assertThatThrownBy(
                        () ->
                                mobRepository
                                        .findMobInstance(mobMotion.getMobInstanceId())
                                        .blockingGet())
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void testGetMobsNearby() {
        Monster mob1 = createMobInstance(mobInstanceId1, 10, 10, 10);
        Monster mob2 = createMobInstance(mobInstanceId2, 50, 50, 50);
        Monster mob3 = createMobInstance(mobInstanceId3, 5000, 5000, 5000);

        mobRepository.insertMobInstance(mob1).blockingGet();
        mobRepository.insertMobInstance(mob2).blockingGet();
        mobRepository.insertMobInstance(mob3).blockingGet();

        List<Monster> mobsNearby =
                mobRepository.getMobsNearby(new Location(TEST_MAP, 0, 0, 0)).blockingGet();

        assertThat(mobsNearby).hasSize(2).containsExactlyInAnyOrder(mob1, mob2);
    }

    private Monster createMobInstance(String mobInstanceId, int x, int y, int z) {
        Motion motion = new Motion();
        motion.setMap(TEST_MAP);
        motion.setX(x);
        motion.setY(y);
        motion.setZ(z);
        Monster monster = new Monster();
        monster.setMotion(motion);
        monster.setMobInstanceId(mobInstanceId);
        monster.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MILLIS));

        return monster;
    }
}
