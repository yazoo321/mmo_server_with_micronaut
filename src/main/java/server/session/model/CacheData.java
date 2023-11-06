package server.session.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import server.common.dto.Motion;

import java.io.Serializable;
import java.time.Instant;

@Data
@AllArgsConstructor
public class CacheData<T> implements Serializable {

    T data;
    boolean requireSync;
    Instant lastUpdated;

}
