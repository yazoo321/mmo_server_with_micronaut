package server.common.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

@ConfigurationProperties("mongo-database")
@Data
public class MongoConfiguration {

    private String databaseName;
    private String playerCharacterCollection;
    private String itemsCollection;
    private String itemInstancesCollection;
    private String droppedItemsCollection;
    private String inventoryCollection;
    private String equipCollection;
    private String playerMotion;
    private String mobInstance;
    private String actorStats;
    private String actorStatus;
    private String actorSkills;
    private String actionbar;
    private String actorAllegiance;
    private String actorThreat;
    private String actorTalents;
}
