package server.attribute.talents.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TalentData {

    String actorId;
    Map<String, Integer> talentLevels;
    Map<String, Talent> talents;
    TalentTree talentTree;
    ActorTalents actorTalents;
}
