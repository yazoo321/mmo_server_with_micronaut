package server.skills.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import server.attribute.stats.model.Stats;
import server.attribute.status.model.ActorStatus;
import server.combat.model.CombatData;

@Data
@Builder
@AllArgsConstructor
public class SkillDependencies {

    String actorId;
    String targetActorId;

    Stats actorStats;
    Stats targetStats;

    ActorStatus actorStatus;
    ActorStatus targetStatus;

    SkillTarget skillTarget;
    CombatData combatData;
}
