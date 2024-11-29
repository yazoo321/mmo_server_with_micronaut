package server.combat.model;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Serdeable
public class ThreatUpdate {

    String actorId;
    Map<String, Integer> addThreat;
    List<String> removeThreat;
}
