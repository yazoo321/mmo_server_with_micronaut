package server.monster.server_integration.model;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Motion;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Serdeable
public class MobUpdate {

    String mobId;

    String mobInstanceId;

    Motion motion;

    String state;

    String target;
}
