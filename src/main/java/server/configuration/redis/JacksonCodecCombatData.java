package server.configuration.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.codec.RedisCodec;
import server.combat.model.CombatData;
import server.common.dto.Motion;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

public class JacksonCodecCombatData implements RedisCodec<String, CombatData> {

    private ObjectMapper objectMapper;

    public JacksonCodecCombatData(ObjectMapper objectMapper) {
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
    public CombatData decodeValue(final ByteBuffer bytes) {
        try {
            byte[] dataBytes = new byte[bytes.remaining()];
            bytes.get(dataBytes);

            return objectMapper.readValue(dataBytes, CombatData.class);
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
    public ByteBuffer encodeValue(CombatData value) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(value);
            return ByteBuffer.wrap(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
