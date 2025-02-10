package server.attribute.talents.model;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Serdeable
@NoArgsConstructor
@AllArgsConstructor
public class TalentTree {

    protected String name;
    protected String description;
    protected Map<Integer, List<Talent>> tieredTalents;
}
