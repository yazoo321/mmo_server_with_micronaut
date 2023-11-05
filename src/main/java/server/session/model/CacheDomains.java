package server.session.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CacheDomains {
    MOTION("MOTION"),
    INVENTORY("INVENTORY");

    public final String domain;
}
