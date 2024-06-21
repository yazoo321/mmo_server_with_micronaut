package server.session.cache;


import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.CachePut;
import io.micronaut.cache.annotation.Cacheable;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Motion;
import server.socket.model.UdpAddressHolder;

@Slf4j
@Singleton
@CacheConfig("udp-session-cache")
public class UdpSessionCache {

    private static final String UDP_SESSION_CACHE = "udp-session-cache";


    @Cacheable(value = UDP_SESSION_CACHE, parameters = "actorId")
    public UdpAddressHolder fetchUdpSession(String actorId) {
        String error = "UDP session was not present in cache!";
        log.error(error);
        throw new RuntimeException(error);
    }

    @CachePut(value = UDP_SESSION_CACHE, parameters = "actorId")
    public UdpAddressHolder setUdpSession(String actorId, UdpAddressHolder addressHolder) {
        // simply put it to cache
        return addressHolder;
    }


}
