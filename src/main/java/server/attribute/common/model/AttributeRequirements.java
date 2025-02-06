package server.attribute.common.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributeRequirements {

    private Map<String, Integer> requirements;
    private Map<String, Integer> dependencies;
}
