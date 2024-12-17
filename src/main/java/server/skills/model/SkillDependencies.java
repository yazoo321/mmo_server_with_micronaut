package server.skills.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import server.attribute.stats.model.Stats;
import server.attribute.status.model.ActorStatus;

@Data
@AllArgsConstructor
public class SkillDependencies {

    Stats actorStats;
    Stats targetStats;

    ActorStatus actorStatus;
    ActorStatus targetStatus;
}
