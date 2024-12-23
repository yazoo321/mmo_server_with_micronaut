package server.attribute.status.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import io.reactivex.rxjava3.core.Single;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import server.attribute.stats.model.Stats;
import server.attribute.stats.service.StatsService;
import server.attribute.status.model.derived.*;
import server.attribute.status.producer.StatusProducer;
import server.attribute.status.service.StatusService;
import server.skills.model.SkillDependencies;

@Data
@Slf4j
@Introspected
@NoArgsConstructor
@BsonDiscriminator
@ReflectiveAccess
@Serdeable
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "category")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Bleeding.class, name = "BLEEDING"),
    @JsonSubTypes.Type(value = Burning.class, name = "BURNING"),
    @JsonSubTypes.Type(value = Dead.class, name = "DEAD"),
    @JsonSubTypes.Type(value = Silenced.class, name = "SILENCED"),
    @JsonSubTypes.Type(value = Stunned.class, name = "STUNNED"),
    @JsonSubTypes.Type(value = Unconscious.class, name = "UNCONCIOUS"),
})
public class Status {

    String id;
    Map<String, Double> derivedEffects;
    Set<String> statusEffects;
    Instant added;
    Instant expiration;
    Boolean canStack;
    String origin;
    String category;

    @JsonIgnore
    protected StatusProducer statusProducer;

    @JsonIgnore
    public boolean requiresDamageApply() {
        return false;
    }

    @JsonIgnore
    public Single<Boolean> apply(
            String actorId, StatusService statusService, StatusProducer statusProducer) {
        // requires override

        return Single.just(false);
    }

    @JsonIgnore
    protected Single<Boolean> baseApply(
            String actorId,
            StatusService statusService,
            Consumer<SkillDependencies> applier,
            StatusProducer statusProducer) {
        // first check if the status is present
        Single<ActorStatus> actorStatuses = statusService.getActorStatus(actorId);
        this.statusProducer = statusProducer;

        return actorStatuses
                .doOnError(err -> log.error("Failed to get actor statuses in apply: {}", err.getMessage()))
                .map(statuses -> {
                    if (statuses.getActorStatuses() == null || statuses.getActorStatuses().isEmpty()) {
                        log.info("actor statuses are null or empty, base apply skipping on {}", this.getCategory());
                        return false;
                    }

                    boolean found = statuses.getActorStatuses().stream().map(Status::getId)
                            .collect(Collectors.toSet()).contains(this.getId());

                    if (!found) {
                        log.info("status missing from actor statuses, base apply skipping on {}", this.getCategory());
                        return false;
                    }

                    applier.accept(SkillDependencies.builder()
                                    .actorId(actorId)
                                    .targetActorId(this.getOrigin())
                            .build());

                    return true;

                });

//        return Single.zip(
//                actorStatuses,
//                (statuses, targetStats, originStats) -> {
//                    if (statuses.getActorStatuses() == null || statuses.getActorStatuses().isEmpty()) {
//                        log.info("actor statuses are null or empty, base apply skipping on {}", this.getCategory());
//                        return false;
//                    }
//
//                    boolean found = statuses.getActorStatuses().stream().map(Status::getId)
//                            .collect(Collectors.toSet()).contains(this.getId());
//
//                    if (!found) {
//                        log.info("status missing from actor statuses, base apply skipping on {}", this.getCategory());
//                        return false;
//                    }
//
//                    applier.accept(new SkillDependencies(originStats, targetStats, null, statuses));
//
//                    return true;
//                });
    }
}
