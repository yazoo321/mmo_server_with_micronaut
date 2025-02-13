package server.skills.listener;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.DamageUpdateMessage;
import server.common.uuid.UUIDHelper;
import server.skills.service.CombatSkillsService;

@Slf4j
@KafkaListener(
        groupId = "single-talent-listener",
        offsetReset = OffsetReset.LATEST,
        offsetStrategy = OffsetStrategy.SYNC,
        clientId = "single-talent-listener")
public class SingleSkillsListener {

    @Inject CombatSkillsService combatSkillsService;

    @Topic("notify-actor-death")
    void receive_actor_death_notify(DamageUpdateMessage damageUpdateMessage) {
        String actorId = damageUpdateMessage.getTargetStats().getActorId();
        if (!UUIDHelper.isPlayer(actorId)) {
            combatSkillsService.deleteActorSkills(
                    damageUpdateMessage.getTargetStats().getActorId());
        }
    }
}
