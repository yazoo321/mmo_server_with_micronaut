package server.monster.server_integration.listener;

import io.micronaut.configuration.kafka.annotation.*;
import jakarta.inject.Inject;
import server.monster.server_integration.model.MobUpdate;
import server.monster.server_integration.service.MonsterDeathService;

@KafkaListener(groupId = "mmo-server", offsetReset = OffsetReset.EARLIEST)
public class MonsterServerListener {

    @Inject MonsterDeathService monsterDeathService;

    @Topic("test")
    public void receive(String data) {
        System.out.println("got some data: " + data);
    }

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

        if (mobUpdate.getState().equalsIgnoreCase("DEATH")) {
            monsterDeathService.handleMonsterDeath(mobUpdate);
        }
    }
}
