package server.utils;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

@ConfigurationProperties("feature-flags")
@Data
public class FeatureFlag {

    private Boolean enableUdp;
}
