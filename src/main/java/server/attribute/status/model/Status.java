package server.attribute.status.model;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

import java.util.Map;

@Data
@Serdeable
public class Status {

    String statusType;
    Map<String, Double> statusEffects;

}
