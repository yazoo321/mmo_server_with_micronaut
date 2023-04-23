package server.scheduled.kafka;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import java.util.UUID;

import jdk.jfr.Enabled;
import server.monster.server_integration.model.MobUpdate;
import server.monster.server_integration.producer.TestProducer;

@Singleton
public class TestKafkaService {

    TestProducer testProducer;

    public TestKafkaService(@KafkaClient("test_client") TestProducer testProducer) {
        // advised this way vs direct inject
        this.testProducer = testProducer;
    }

//    @Scheduled(fixedDelay = "10s")
    void executeEveryTen() {
        MobUpdate mobUpdate =
                new MobUpdate(
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString(),
                        null,
                        "ATTACKING",
                        "player1");
        testProducer.sendFakeUpdate(mobUpdate);
    }
}
