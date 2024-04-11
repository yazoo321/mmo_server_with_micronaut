package server.common.configuration.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.codec.RedisCodec;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import server.common.dto.Motion;

public class JacksonCodecMotion implements RedisCodec<String, Motion> {

    private final ObjectMapper objectMapper;

    public JacksonCodecMotion(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String decodeKey(final ByteBuffer bytes) {
        try {
            return objectMapper.readValue(bytes.array(), String.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Motion decodeValue(final ByteBuffer bytes) {
        try {
            byte[] dataBytes = new byte[bytes.remaining()];
            bytes.get(dataBytes);

            return objectMapper.readValue(dataBytes, Motion.class);
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
