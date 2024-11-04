package server.faction.service;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server.faction.model.ActorAllegiance;
import server.faction.model.HostileAllegiance;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ActorHostilityServiceTest {

    @Mock
    private ActorAllegianceService actorAllegianceService;

    @Mock
    private HostileAllegianceService hostileAllegianceService;

    @InjectMocks
    private ActorHostilityService actorHostilityService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void evaluateActorHostilityStatus_shouldReturnMinimumHostilityLevel_whenHostilitiesExist() {
        // Given actor and target IDs
        String actorId = "actor1";
        String targetId = "actor2";

        // Given allegiances for actor and target
        List<ActorAllegiance> actorAllegiances = List.of(new ActorAllegiance(actorId, "GuildA"));
        List<ActorAllegiance> targetAllegiances = List.of(new ActorAllegiance(targetId, "GuildB"));

        // Given hostilities between allegiances
        List<HostileAllegiance> hostileAllegiances = List.of(
                new HostileAllegiance("GuildA", "GuildB", 5),
                new HostileAllegiance("GuildA", "GuildC", 3)
        );

        // Mock behavior for actor allegiance service
        when(actorAllegianceService.getActorAllegiance(actorId)).thenReturn(Single.just(actorAllegiances));
        when(actorAllegianceService.getActorAllegiance(targetId)).thenReturn(Single.just(targetAllegiances));

        // Mock behavior for hostile allegiance service
        when(hostileAllegianceService.getHostilities(List.of("GuildA"))).thenReturn(Single.just(hostileAllegiances));

        // When
        Integer hostilityLevel = actorHostilityService.evaluateActorHostilityStatus(actorId, targetId).blockingGet();

        // Then
        assertThat(hostilityLevel).isEqualTo(5);
    }

    @Test
    void evaluateActorHostilityStatus_shouldReturnZero_whenNoHostilitiesExist() {
        // Given actor and target IDs
        String actorId = "actor1";
        String targetId = "actor2";

        // Given allegiances for actor and target
        List<ActorAllegiance> actorAllegiances = List.of(new ActorAllegiance(actorId, "GuildA"));
        List<ActorAllegiance> targetAllegiances = List.of(new ActorAllegiance(targetId, "GuildD"));

        // Given no hostile relationships between actor and target allegiances
        List<HostileAllegiance> hostileAllegiances = List.of(
                new HostileAllegiance("GuildA", "GuildC", 3)
        );

        // Mock behavior for actor allegiance service
        when(actorAllegianceService.getActorAllegiance(actorId)).thenReturn(Single.just(actorAllegiances));
        when(actorAllegianceService.getActorAllegiance(targetId)).thenReturn(Single.just(targetAllegiances));

        // Mock behavior for hostile allegiance service
        when(hostileAllegianceService.getHostilities(List.of("GuildA"))).thenReturn(Single.just(hostileAllegiances));

        // When
        Integer hostilityLevel = actorHostilityService.evaluateActorHostilityStatus(actorId, targetId).blockingGet();

        // Then
        assertThat(hostilityLevel).isEqualTo(0);
    }

    @Test
    void evaluateActorHostilityStatus_shouldReturnZero_whenActorHasNoAllegiances() {
        // Given actor and target IDs
        String actorId = "actor1";
        String targetId = "actor2";

        // Actor has no allegiances
        List<ActorAllegiance> actorAllegiances = List.of();
        List<ActorAllegiance> targetAllegiances = List.of(new ActorAllegiance(targetId, "GuildB"));

        // No hostile allegiances due to actor's lack of allegiances
        List<HostileAllegiance> hostileAllegiances = List.of();

        // Mock behavior for actor allegiance service
        when(actorAllegianceService.getActorAllegiance(actorId)).thenReturn(Single.just(actorAllegiances));
        when(actorAllegianceService.getActorAllegiance(targetId)).thenReturn(Single.just(targetAllegiances));

        // Mock behavior for hostile allegiance service
        when(hostileAllegianceService.getHostilities(List.of())).thenReturn(Single.just(hostileAllegiances));

        // When
        Integer hostilityLevel = actorHostilityService.evaluateActorHostilityStatus(actorId, targetId).blockingGet();

        // Then
        assertThat(hostilityLevel).isEqualTo(0);
    }
}

