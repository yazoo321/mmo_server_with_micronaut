package server.configuration.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.codec.RedisCodec;
import server.common.dto.Motion;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

public class JacksonRedisCodecMotion implements RedisCodec<String, Motion> {

    private ObjectMapper objectMapper;

    public JacksonRedisCodecMotion(ObjectMapper objectMapper) {
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
    public Motion decodeValue(ByteBuffer bytes) {
        try {
            return objectMapper.readValue(bytes.array(), Motion.class);
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
    public ByteBuffer encodeValue(Motion value) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(value);
            return ByteBuffer.wrap(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}