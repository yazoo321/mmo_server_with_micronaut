package server.monster.server_integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Motion;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MobUpdate {

    String mobId;

    String mobInstanceId;

    Motion motion;

    String state;

    String target;
}
