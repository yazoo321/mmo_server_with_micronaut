package server.session.model;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Serdeable
@AllArgsConstructor
public class CacheData<T> {

    T data;
}
