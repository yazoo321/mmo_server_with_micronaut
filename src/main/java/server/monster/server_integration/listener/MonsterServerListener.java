package server.monster.server_integration.listener;

import io.micronaut.configuration.kafka.annotation.*;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.monster.server_integration.model.Monster;
import server.monster.server_integration.producer.MonsterServerProducer;
import server.monster.server_integration.service.MobInstanceService;

@Slf4j
@KafkaListener(
        groupId = "mmo-server",
        offsetReset = OffsetReset.EARLIEST,
        offsetStrategy = OffsetStrategy.SYNC,
        clientId = "mob_repo_client")
public class MonsterServerListener {

    MonsterServerProducer monsterServerProducer;

    public MonsterServerListener(
            @KafkaClient("mob-server-client") MonsterServerProducer monsterServerProducer) {
        this.monsterServerProducer = monsterServerProducer;
    }

    @Inject MobInstanceService mobInstanceService;

    @Topic("create-mob")
    public void receiveCreateMob(Monster monster) {
        mobInstanceService
                .createMob(monster)
                .doOnError(error -> log.error("Error on creating mob, {}", error.getMessage()))
                .subscribe();
    }

    @Topic("mob-motion-update")
    public void receiveUpdateMob(Monster monster) {
        mobInstanceService
                .updateMobMotion(monster.getMobInstanceId(), monster.getMotion())
                .doOnError(
                        error ->
                                log.error(
                                        "Error processing mob motion update, {}",
                                        error.getMessage()))
                .subscribe();
        monsterServerProducer.sendMobUpdateResult(monster);
    }
}
