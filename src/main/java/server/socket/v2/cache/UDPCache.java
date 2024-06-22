package server.socket.v2.cache;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UDPCache {

    UPDATED_AT("updated_at"),
    ADDRESS("address");

    public final String type;
}
