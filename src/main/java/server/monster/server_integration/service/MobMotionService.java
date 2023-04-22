package server.monster.server_integration.service;

import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import server.common.dto.Location;
import server.monster.server_integration.model.MobMotion;
import server.monster.server_integration.repository.MobMotionRepository;

@Slf4j
@Service
public class MobMotionService {

    @Inject MobMotionRepository mobMotionRepository;

    public Single<List<MobMotion>> getMobsNearby(Location location) {
        return mobMotionRepository.getMobsNearby(location);
    }

    public Single<MobMotion> getPlayerMotion(String mobInstanceId) {
        return mobMotionRepository.findMobMotion(mobInstanceId);
    }

    public void createMob(String mobId) {}
}
