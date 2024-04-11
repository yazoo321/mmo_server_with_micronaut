package server.session.cache;

import jakarta.inject.Singleton;

@Singleton
public class ActorMotionCache {
    //
    //    private final String ACTOR_MOTION_CACHE = "actorMotionCache";
    //
    //    @Inject
    //    PlayerMotionRepository playerMotionRepository;
    //
    //    @Cacheable(value = ACTOR_MOTION_CACHE, parameters = {"actorId"})
    //    public Motion getActorMotion(String actorId) {
    //        // if not present, which it should be:
    //        if (UUIDHelper.isPlayer(actorId)) {
    //            playerMotionRepository.fetchPlayerMotion(actorId);
    //        } else {
    //
    //        }
    //    }
    //
    //    @CachePut(value = ACTOR_MOTION_CACHE, parameters = {"actorId"})
    //    public void updateActorMotion(String actorId, Motion motion) {
    //    }
}
