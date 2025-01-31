package server.attribute.talents.model;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class TalentTree {

    protected String name;
    protected String description;
    protected Map<Integer, List<Talent>> tieredTalents;
}
