package server.actionbar.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActorActionbar {

    String actorId;
    String actionbarId;

    ActionbarContent actionbarContent;
}
