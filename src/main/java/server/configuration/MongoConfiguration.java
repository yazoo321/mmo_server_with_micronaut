package server.configuration;

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
    private String playerAttributes;

}
