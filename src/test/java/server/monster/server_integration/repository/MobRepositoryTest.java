package server.monster.server_integration.repository;

import static org.assertj.core.api.Assertions.*;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.monster.server_integration.model.Monster;

@MicronautTest
public class MobRepositoryTest {

    @Inject MobRepository mobRepository;

    private static final String TEST_MAP = "TEST_MAP";

    private static final String actorId1 = "5068a37d-46bc-40f2-9179-e9689bbfa52b";
    private static final String actorId2 = "5068a37d-46bc-40f2-9179-e9689bbfa52c";
    private static final String actorId3 = "5068a37d-46bc-40f2-9179-e9689bbfa52d";

    @BeforeEach
    void deleteTestData() {
        mobRepository.deleteMobInstance(actorId1).blockingGet();
        mobRepository.deleteMobInstance(actorId2).blockingGet();
        mobRepository.deleteMobInstance(actorId3).blockingGet();
    }

    @Test
    void testFindMobMotion() {
        // Create a MobMotion object and insert it into the repository
        Monster mobMotion = createMobInstance(actorId1, 1000, 1000, 1000);
        mobRepository.insertMobInstance(mobMotion).blockingGet();

        // Retrieve the MobMotion by its instance ID
        Monster result = mobRepository.findMobInstance(mobMotion.getActorId()).blockingGet();

        // Verify that the retrieved MobMotion matches the inserted one
        assertThat(result).usingRecursiveComparison().isEqualTo(mobMotion);
    }

    @Test
    void testInsertMobMotion() {
        // Create a MobMotion object and insert it into the repository
        Monster mobMotion = createMobInstance(actorId1, 1000, 1000, 1000);
        mobRepository.insertMobInstance(mobMotion).blockingGet();

        // Retrieve the MobMotion by its instance ID
        Single<Monster> result = mobRepository.findMobInstance(mobMotion.getActorId());

        // Verify that the retrieved MobMotion matches the inserted one
        assertThat(result.blockingGet()).usingRecursiveComparison().isEqualTo(mobMotion);
    }

    @Test
    void testUpdateMotionOnly() {
        // Create a MobMotion object and insert it into the repository
        Monster mob = createMobInstance(actorId1, 1000, 1000, 1000);
        mobRepository.insertMobInstance(mob).blockingGet();

        // Update the motion of the MobMotion object
        mob.getMotion().setX(42);
        mob.getMotion().setY(84);
        mobRepository.updateMotionOnly(mob.getActorId(), mob.getMotion()).blockingGet();

        // Retrieve the updated MobMotion by its instance ID
        Monster result = mobRepository.findMobInstance(mob.getActorId()).blockingGet();

        // Verify that the retrieved MobMotion matches the updated one
        assertThat(result).usingRecursiveComparison().ignoringFields("updatedAt").isEqualTo(mob);
        assertThat(result.getUpdatedAt())
                .isCloseTo(mob.getUpdatedAt(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    void testDeleteMobInstance() {
        // Create a MobMotion object and insert it into the repository
        Monster mobMotion = createMobInstance(actorId1, 1000, 1000, 1000);
        mobRepository.insertMobInstance(mobMotion).blockingGet();

        // Delete the MobMotion object by its instance ID
        mobRepository.deleteMobInstance(mobMotion.getActorId()).blockingGet();

        // Attempt to retrieve the deleted MobMotion by its instance ID
        // Verify that the retrieved result is null
        assertThatThrownBy(
                        () -> mobRepository.findMobInstance(mobMotion.getActorId()).blockingGet())
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void testGetMobsNearby() {
        Monster mob1 = createMobInstance(actorId1, 10, 10, 10);
        Monster mob2 = createMobInstance(actorId2, 50, 50, 50);
        Monster mob3 = createMobInstance(actorId3, 25000, 25000, 25000);

        mobRepository.insertMobInstance(mob1).blockingGet();
        mobRepository.insertMobInstance(mob2).blockingGet();
        mobRepository.insertMobInstance(mob3).blockingGet();

        List<Monster> mobsNearby =
                mobRepository.getMobsNearby(new Location(TEST_MAP, 0, 0, 0)).blockingGet();

        assertThat(mobsNearby).hasSize(2).containsExactlyInAnyOrder(mob1, mob2);
    }

    private Monster createMobInstance(String actorId, int x, int y, int z) {
        Motion motion = new Motion();
        motion.setMap(TEST_MAP);
        motion.setX(x);
        motion.setY(y);
        motion.setZ(z);
        Monster monster = new Monster();
        monster.setMotion(motion);
        monster.setActorId(actorId);
        monster.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MILLIS));

        return monster;
    }
}
