package server.monster.server_integration.service;

import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.monster.server_integration.model.Monster;
import server.monster.server_integration.repository.MobRepository;

@Slf4j
@Service
public class MobInstanceService {

    @Inject MobRepository mobRepository;

    public Single<List<Monster>> getMobsNearby(Location location) {
        return mobRepository.getMobsNearby(location);
    }

    public void createMob(String mobId, Motion motion) {
        Monster mob = new Monster();
        mob.setMobInstanceId(UUID.randomUUID().toString());
        mob.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        mob.setMotion(motion);
        mob.setMobId(mobId);

        mobRepository.insertMobInstance(mob);

        // also need to create mob attributes
    }

    public void updateMobMotion(String mobInstanceId, Motion motion) {
        mobRepository.updateMotionOnly(mobInstanceId, motion);
    }
}
