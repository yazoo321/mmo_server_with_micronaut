package server.session.model;

import lombok.Data;

@Data
public class CacheKey {

    String domain;
    String id;

    public static String of(CacheDomains domain, String id) {
        return domain.getDomain() + "_" + id;
    }

    public String getKey() {
        return domain + "_" + id;
    }
}
