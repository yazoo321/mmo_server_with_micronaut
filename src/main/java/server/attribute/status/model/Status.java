package server.attribute.status.model;

import lombok.Data;

import java.util.Map;

@Data
public class Status {

    String statusType;
    Map<String, Double> statusEffects;

}
