package server.common.dto;


import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Serdeable
@ReflectiveAccess
@NoArgsConstructor
public class NumTag {

    public NumTag(
           String name,
           Integer value) {
        this.name = name;
        this.value = value;
    }
    // Key value pair
    String name;
    Integer value;

}
