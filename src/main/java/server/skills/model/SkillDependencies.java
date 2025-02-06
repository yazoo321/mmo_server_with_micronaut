package server.skills.model;

import io.micronaut.websocket.WebSocketSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.attribute.stats.model.Stats;
import server.attribute.status.model.ActorStatus;
import server.combat.model.CombatData;
import server.common.dto.Motion;
import server.items.equippable.model.EquippedItems;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkillDependencies {

    String actorId;
    String targetActorId;

    Stats actorStats;
    Stats targetStats;

    ActorStatus actorStatus;
    ActorStatus targetStatus;

    Motion actorMotion;
    // TODO: should this perhaps be a Map<String, Motion> in case we have multiple targets
    Motion targetMotion;

    SkillTarget skillTarget;
    CombatData combatData;

    Map<String, EquippedItems> equippedItems;

    WebSocketSession session;
}
