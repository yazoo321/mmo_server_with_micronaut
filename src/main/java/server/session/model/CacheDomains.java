package server.session.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CacheDomains {

    CLIENT_ADDRESS("CLIENT_ADDRESS"),
    MOTION("MOTION"),
    COMBAT_DATA("COMBAT_DATA"),
    INVENTORY("INVENTORY");

    public final String domain;
}
