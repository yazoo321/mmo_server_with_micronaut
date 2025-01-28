package server.attribute.talents.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TalentTree {

    protected String name;
    protected String description;
    protected Map<Integer, List<Talent>> tieredTalents;
}
