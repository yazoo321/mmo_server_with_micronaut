package server.monster.server_integration.listener;

import io.micronaut.configuration.kafka.annotation.*;
import server.monster.server_integration.model.MobUpdate;

@KafkaListener(groupId = "mmo-server", offsetReset = OffsetReset.EARLIEST)
public class MonsterServerListener {

    @Topic("mob-updates")
    public void receive(MobUpdate mobUpdate) {
        System.out.printf(
                "Got a mob update, mob id: %s, mob instance id: %s, mote: %s, state: %s, target:"
                        + " %s%n",
                mobUpdate.getMobId(),
                mobUpdate.getMobInstanceId(),
                mobUpdate.getMotion(),
                mobUpdate.getState(),
                mobUpdate.getTarget());
    }
}
