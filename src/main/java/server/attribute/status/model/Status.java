package server.attribute.status.model;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

import java.util.Map;

@Data
@Serdeable
@ReflectiveAccess
public class Status {

    String statusType;
    Map<String, Double> statusEffects;

}
