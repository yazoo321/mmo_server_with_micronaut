package server.attribute.talents.model;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Serdeable
@NoArgsConstructor
@AllArgsConstructor
public class TalentList {
    // This class is added because UE struct does not support List<Talent> in Map struct, so we need
    // to split

    private List<Talent> talentList;
}
