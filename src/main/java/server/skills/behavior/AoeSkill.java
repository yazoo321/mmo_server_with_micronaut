package server.skills.behavior;

import io.reactivex.rxjava3.core.Single;
import server.skills.model.SkillTarget;

import java.util.List;

public interface AoeSkill {


    Single<List<String>> getAffectedActors(SkillTarget skillTarget);

}
