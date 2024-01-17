package server.common.dto;


import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Serdeable
@ReflectiveAccess
public class Tag {

    public Tag(
            String name,
            String value) {
        this.name = name;
        this.value = value;
    }
    // Key value pair
    String name;
    String value;

}
