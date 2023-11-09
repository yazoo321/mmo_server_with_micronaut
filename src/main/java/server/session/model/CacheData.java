package server.session.model;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import server.common.dto.Motion;

import java.io.Serializable;
import java.time.Instant;

@Data
@Serdeable
@AllArgsConstructor
public class CacheData<T> {

    T data;

}
