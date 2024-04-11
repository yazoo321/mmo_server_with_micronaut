package server.common.configuration.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.codec.RedisCodec;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import server.combat.model.CombatData;
import server.skills.available.destruction.fire.Fireball;
import server.skills.available.restoration.heals.BasicHeal;

public class JacksonCodecCombatData implements RedisCodec<String, CombatData> {

    private final ObjectMapper objectMapper;

    public JacksonCodecCombatData(ObjectMapper objectMapper) {
        objectMapper.registerSubtypes(Fireball.class, BasicHeal.class);

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
