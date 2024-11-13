package server.skills.behavior;

import io.reactivex.rxjava3.core.Single;
import server.common.dto.Location;

import java.util.List;

public interface AoeSkill {


    Single<List<String>> getAffectedActors(Location location, String casterId);

}
