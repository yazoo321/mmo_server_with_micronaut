package server.monster.server_integration.service;

import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import server.common.dto.Location;
import server.monster.server_integration.model.Monster;
import server.monster.server_integration.repository.MobRepository;

@Slf4j
@Service
public class MobMotionService {

    @Inject MobRepository mobMotionRepository;

    public Single<List<Monster>> getMobsNearby(Location location) {
        return mobMotionRepository.getMobsNearby(location);
    }

    public Single<Monster> getPlayerMotion(String mobInstanceId) {
        return mobMotionRepository.findMobMotion(mobInstanceId);
    }

    public void createMob(String mobId) {}
}
