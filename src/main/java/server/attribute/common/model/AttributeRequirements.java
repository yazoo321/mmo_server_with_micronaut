package server.attribute.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributeRequirements {

    private Map<String, Integer> requirements;
    private List<String> dependencies;
}
