package server.motion.repository;

import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.motion.model.SpawnLocation;
import server.motion.service.PlayerMotionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Singleton
public class RespawnPoints {

//    Not required to push into repository, can just keep this data statically, until we have too many data points

    Map<SpawnLocation, List<Location>> spawnLocations = new HashMap<>();

    public RespawnPoints() {
        SpawnLocation tooksworthTown = new SpawnLocation("tooksworth", "town");
        Location tookworthTownRespawn = new Location(PlayerMotionService.STARTING_MOTION);

        this.spawnLocations.put(tooksworthTown, List.of(tookworthTownRespawn));

        SpawnLocation tooksworthCheckpoint1 = new SpawnLocation("tooksworth", "checkpoint");
        this.spawnLocations.put(tooksworthCheckpoint1, List.of(tookworthTownRespawn));
    }

    public Motion getRespawnPointFor(Motion motion, String type) {
        // In the future, there may not be a respawn point for a given map
        // and we may need to identify nearest map to respawn to
        Location location = getRespawnPointFor(motion.getMap(), type, new Location(motion));

        return Motion.fromLocation(motion.getMap(), location);
    }

    public Location getRespawnPointFor(String map, String type, Location point) {
        if (type.equals("nearest")) {
            Location loc1 = getRespawnPointFor(map, "town", point);
            Location loc2 = getRespawnPointFor(map, "checkpoint", point);

            return Location.findNearestLocation(point, List.of(loc1, loc2)).get();
        }

        List<Location> locations = spawnLocations.get(new SpawnLocation(map, type));
        Optional<Location> spawnLoc = Location.findNearestLocation(point, locations);

        if (spawnLoc.isEmpty()) {
            log.error("No spawn location found! {}, {}", map, type);
            throw new RuntimeException("No spawn location found!");
        }

        return spawnLoc.get();
    }

}
