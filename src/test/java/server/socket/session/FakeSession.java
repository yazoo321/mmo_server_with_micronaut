package server.socket.session;

import io.micronaut.context.annotation.Bean;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.http.MediaType;
import io.micronaut.websocket.CloseReason;
import io.micronaut.websocket.WebSocketSession;
import org.reactivestreams.Publisher;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Bean
public class FakeSession implements WebSocketSession {

    private Map<String, Object> dataMap = new HashMap<>();

    @Override
    public String getId() {
        return "1";
    }

    @Override
    public MutableConvertibleValues<Object> getAttributes() {
        return null;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public Set<? extends WebSocketSession> getOpenSessions() {
        return null;
    }

    @Override
    public URI getRequestURI() {
        return null;
    }

    @Override
    public String getProtocolVersion() {
        return null;
    }

    @Override
    public <T> Publisher<T> send(T message, MediaType mediaType) {
        return null;
    }

    @Override
    public <T> CompletableFuture<T> sendAsync(T message, MediaType mediaType) {
        return null;
    }

    @Override
    public void close() {}

    @Override
    public void close(CloseReason closeReason) {}

    @Override
    public MutableConvertibleValues<Object> put(CharSequence key, @Nullable Object value) {
        dataMap.put(key.toString(), value);
        return MutableConvertibleValues.of(dataMap);
    }

    @Override
    public MutableConvertibleValues<Object> remove(CharSequence key) {
        dataMap.remove(key.toString());
        return MutableConvertibleValues.of(dataMap);
    }

    @Override
    public MutableConvertibleValues<Object> clear() {
        dataMap.clear();
        return MutableConvertibleValues.of(dataMap);
    }

    @Override
    public Set<String> names() {
        return null;
    }

    @Override
    public Collection<Object> values() {
        return null;
    }

    @Override
    public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
        return Optional.empty();
    }

    public Map<String, Object> asMap() {
        return dataMap;
    }
}
