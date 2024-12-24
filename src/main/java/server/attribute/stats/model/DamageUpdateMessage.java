package server.attribute.stats.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DamageUpdateMessage {

    private DamageSource damageSource;
    private Stats targetStats;
    private Stats originStats;

}
