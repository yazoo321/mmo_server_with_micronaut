package server.common.configuration.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.codec.RedisCodec;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import server.session.model.CacheData;

public class CustomJacksonRedisCodec implements RedisCodec<String, CacheData> {

    private ObjectMapper objectMapper;

    public CustomJacksonRedisCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String decodeKey(ByteBuffer bytes) {
        try {
            return objectMapper.readValue(bytes.array(), String.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public CacheData decodeValue(ByteBuffer bytes) {
        try {
            return objectMapper.readValue(bytes.array(), CacheData.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public ByteBuffer encodeKey(String key) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(key);
            return ByteBuffer.wrap(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public ByteBuffer encodeValue(CacheData value) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(value);
            return ByteBuffer.wrap(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
