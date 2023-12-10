package server.session.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CacheDomains {
    MOTION("MOTION"),
    COMBAT_DATA("COMBAT_DATA"),
    INVENTORY("INVENTORY");

    public final String domain;
}
