package server.skills.behavior;

import io.reactivex.rxjava3.core.Single;
import java.util.List;
import server.skills.model.SkillTarget;

public interface AoeSkill {

    Single<List<String>> getAffectedActors(SkillTarget skillTarget);
}
