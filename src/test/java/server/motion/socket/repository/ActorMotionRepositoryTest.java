package server.motion.socket.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.reactivex.rxjava3.core.Single;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import server.common.dto.Motion;
import server.common.uuid.UUIDHelper;
import server.monster.server_integration.model.Monster;
import server.monster.server_integration.repository.MobRepository;
import server.motion.dto.PlayerMotion;
import server.motion.repository.ActorMotionRepository;
import server.motion.repository.PlayerMotionRepository;

public class ActorMotionRepositoryTest {

    @Mock private PlayerMotionRepository playerMotionRepository;

    @Mock private MobRepository mobRepository;

    @InjectMocks private ActorMotionRepository actorMotionRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFetchActorMotion_Player() {
        String playerId = "CHARACTER_1";
        Motion motion = new Motion();
        PlayerMotion playerMotion = new PlayerMotion(playerId, motion, null, null);
        when(playerMotionRepository.fetchPlayerMotion(playerId))
                .thenReturn(Single.just(playerMotion));

        Single<Motion> result = actorMotionRepository.fetchActorMotion(playerId);

        assertThat(result.blockingGet()).isEqualTo(motion);
        verify(playerMotionRepository).fetchPlayerMotion(playerId);
        Mockito.verifyNoInteractions(mobRepository);
    }

    @Test
    public void testFetchActorMotion_Mob() {
        String mobId = UUID.randomUUID().toString();
        Motion motion = new Motion();
        Monster monster = new Monster();
        monster.setMotion(motion);
        when(mobRepository.findMobInstance(mobId)).thenReturn(Single.just(monster));

        Single<Motion> result = actorMotionRepository.fetchActorMotion(mobId);

        assertThat(result.blockingGet()).isEqualTo(motion);
        verify(mobRepository).findMobInstance(mobId);
        Mockito.verifyNoInteractions(playerMotionRepository);
    }

    private static Stream<Arguments> actorIdToMotion() {
        return Stream.of(
                Arguments.of(
                        UUID.randomUUID().toString(),
                        Motion.builder().map("test1").x(10).y(20).z(30).build()),
                Arguments.of(
                        "CHARACTER_1", Motion.builder().map("test1").x(10).y(20).z(30).build()));
    }

    @ParameterizedTest
    @MethodSource("actorIdToMotion")
    public void testUpdateActorMotion(String actorId, Motion motion) {
        // if this is a mob instance
        Monster expectedMob = new Monster();
        expectedMob.setActorId(actorId);
        expectedMob.setMotion(motion);

        // if this is a player instance
        PlayerMotion expectedPlayerMotion = new PlayerMotion();
        expectedPlayerMotion.setActorId(actorId);
        expectedPlayerMotion.setMotion(motion);

        // prepare update requests
        when(playerMotionRepository.updateMotion(anyString(), any(PlayerMotion.class)))
                .thenReturn(Single.just(expectedPlayerMotion.getMotion()));
        when(mobRepository.updateMotionOnly(actorId, motion))
                .thenReturn(Single.just(expectedMob.getMotion()));

        // prepare read requests
        when(playerMotionRepository.fetchPlayerMotion(actorId))
                .thenReturn(Single.just(expectedPlayerMotion));
        when(mobRepository.findMobInstance(actorId)).thenReturn(Single.just(expectedMob));

        Motion updatedMotion = actorMotionRepository.updateActorMotion(actorId, motion);
        assertThat(updatedMotion).isEqualTo(motion);

        if (UUIDHelper.isPlayer(actorId)) {
            verify(playerMotionRepository).updateMotion(anyString(), any(PlayerMotion.class));
            verifyNoInteractions(mobRepository);
        } else {
            verify(mobRepository).updateMotionOnly(eq(actorId), eq(motion));
            verifyNoInteractions(playerMotionRepository);
        }

        Motion resultMotion = actorMotionRepository.fetchActorMotion(actorId).blockingGet();
        assertThat(resultMotion).isEqualTo(motion);

        if (UUIDHelper.isPlayer(actorId)) {
            verify(playerMotionRepository).fetchPlayerMotion(eq(actorId));
            verifyNoInteractions(mobRepository);
        } else {
            verify(mobRepository).findMobInstance(eq(actorId));
            verifyNoInteractions(playerMotionRepository);
        }
    }
}
