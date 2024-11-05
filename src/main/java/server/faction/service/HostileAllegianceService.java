package server.faction.service;

import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import server.faction.model.HostileAllegiance;
import server.faction.repository.HostileAllegianceRepository;

@Singleton
public class HostileAllegianceService {

    @Inject private HostileAllegianceRepository hostileAllegianceRepository;

    Single<List<HostileAllegiance>> getHostilities(List<String> allegianceName) {
        return hostileAllegianceRepository.findHostilitiesByAllegiance(allegianceName);
    }

    Single<HostileAllegiance> addOrUpdateHostility(
            String allegianceName, String hostileTo, int hostilityLevel) {
        return hostileAllegianceRepository.upsertHostility(
                allegianceName, hostileTo, hostilityLevel);
    }
}
