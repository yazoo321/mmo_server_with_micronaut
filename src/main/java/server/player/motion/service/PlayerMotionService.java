package server.player.motion.service;

import server.player.motion.dto.PlayerMotion;
import server.player.motion.dto.PlayerMotionList;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class PlayerMotionService {
    Map<String, PlayerMotion> playerMotions = new HashMap<>();

    public void updatePlayerState(PlayerMotion request) {

        String playerName = request.getPlayerName();
        if (playerMotions.containsKey(playerName)) {
            playerMotions.get(playerName).setMotion(request.getMotion());
        } else {
            playerMotions.put(request.getPlayerName(), request);
        }
    }

    public PlayerMotionList getPlayersNearMe(String playerName) {
        PlayerMotionList playerMotionList = new PlayerMotionList();
        List<PlayerMotion> playersNearMe = new ArrayList<>();
        playerMotions.forEach((name, motion) -> {
            if (!motion.getPlayerName().equals(playerName)) {
                // add more filters, e.g. by distance
                // consider caching
                playersNearMe.add(motion);
            }
        });

        playerMotionList.setPlayerMotionList(playersNearMe);
        return playerMotionList;
    }
}
