package server.player.motion.socket.v1.service;

import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import server.common.dto.Motion;
import server.player.motion.dto.PlayerMotion;
import server.player.motion.dto.exceptions.PlayerMotionException;
import server.player.motion.socket.v1.repository.PlayerMotionRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.function.Predicate;

@Slf4j
@Singleton
public class PlayerMotionService {

    @Inject
    PlayerMotionRepository playerMotionRepository;

    public PlayerMotion initializePlayerMotion(String playerName) {
        // can create custom start points for different classes/maps etc
        PlayerMotion playerMotion = new PlayerMotion();
        Motion startMotion = new Motion();
        startMotion.setX(0);
        startMotion.setY(0);
        startMotion.setZ(0);

        startMotion.setVx(0);
        startMotion.setVy(0);
        startMotion.setVz(0);

        startMotion.setPitch(0);
        startMotion.setRoll(0);
        startMotion.setYaw(0);

        startMotion.setIsFalling(false);

        playerMotion.setMotion(startMotion);
        playerMotion.setPlayerName(playerName);

        playerMotionRepository.insertPlayerMotion(playerMotion);

        return playerMotion;
    }

    public Publisher<PlayerMotion> updatePlayerMotion(String playerName, PlayerMotion motion, WebSocketBroadcaster broadcaster) {
        if (!Objects.equals(playerName, motion.getPlayerName())) {
            log.error("Logged in player {} tried to update motion for player {}", playerName, motion.getPlayerName());
            throw new PlayerMotionException("Tried to update motion for incorrect player name");
        }

        motion.setUpdatedAt(ZonedDateTime.now());
        motion.setIsOnline(true);
        playerMotionRepository.updatePlayerMotion(motion);

        return broadcaster.broadcast(motion, sendToNearbyListenersFilter(motion));
    }

    public Publisher<PlayerMotion> disconnectPlayer(String playerName, WebSocketBroadcaster broadcaster) {
        PlayerMotion motion = playerMotionRepository.findPlayerMotion(playerName);

        motion.setIsOnline(false);

        playerMotionRepository.updatePlayerMotion(motion);

        return broadcaster.broadcast(motion, sendToNearbyListenersFilter(motion));
    }

    private Predicate<WebSocketSession> sendToNearbyListenersFilter(PlayerMotion playerMotion) {
        return s -> {
            // TODO: Add filters to track only nearby motion
            return true;
        };
    }

}
