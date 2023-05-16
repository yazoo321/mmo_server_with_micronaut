package server.motion.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.monster.server_integration.model.Monster;

@Data
@Builder
@JsonInclude
@NoArgsConstructor
@AllArgsConstructor
public class MotionResult {
    // the result is either player motion or monster
    PlayerMotion playerMotion;
    Monster monster;
}
