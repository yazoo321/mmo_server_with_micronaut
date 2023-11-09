package server.configuration.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.codec.RedisCodec;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.runtime.context.scope.ThreadLocal;
import jakarta.inject.Singleton;
import server.session.model.CacheData;

@Factory
public class CustomRedisCodecFactory {

    @Singleton
    @ThreadLocal
    @Replaces(factory = RedisCodec.class)
    public RedisCodec<String, CacheData> customRedisCodec(ObjectMapper objectMapper) {
        return new CustomJacksonRedisCodec(objectMapper);
    }
}
