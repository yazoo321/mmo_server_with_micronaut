package server.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

@ConfigurationProperties("player-character")
@Data
public class PlayerCharacterConfiguration {

    private String databaseName;
    private String collectionName;
}
