package server.attribute.talents.model;

import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Serdeable
@NoArgsConstructor
@AllArgsConstructor
public class TalentTree {

    protected String name;
    protected String description;
    protected Map<Integer, TalentList> tieredTalents;
}
