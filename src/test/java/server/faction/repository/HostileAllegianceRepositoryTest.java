package server.faction.repository;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.faction.model.HostileAllegiance;

@MicronautTest
public class HostileAllegianceRepositoryTest {

    @Inject private HostileAllegianceRepository hostileAllegianceRepository;

    private final String GUILD_A = "GuildA";
    private final String GUILD_B = "GuildB";
    private final String GUILD_C = "GuildC";
    private final int hostilityLevel = 5;

    @BeforeEach
    public void setup() {
        // Clearing any existing data in the test collection
        hostileAllegianceRepository.delete(GUILD_A, GUILD_B).blockingSubscribe();
        hostileAllegianceRepository.delete(GUILD_A, GUILD_C).blockingSubscribe();
    }

    @Test
    public void testFindHostilitiesByAllegiance() {
        List<HostileAllegiance> expected =
                List.of(
                        new HostileAllegiance(GUILD_A, GUILD_B, hostilityLevel),
                        new HostileAllegiance(GUILD_A, GUILD_C, hostilityLevel));

        hostileAllegianceRepository
                .upsertHostility(GUILD_A, GUILD_B, hostilityLevel)
                .blockingSubscribe();
        hostileAllegianceRepository
                .upsertHostility(GUILD_A, GUILD_C, hostilityLevel)
                .blockingSubscribe();

        // Insert hostility record for later retrieval
        List<HostileAllegiance> actual =
                hostileAllegianceRepository
                        .findHostilitiesByAllegiance(List.of(GUILD_A))
                        .blockingGet();

        Assertions.assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }
}
