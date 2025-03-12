package server.skills.available.mage.arcane;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Single;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.types.ClassTypes;
import server.combat.model.CombatData;
import server.combat.model.CombatRequest;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.skills.active.channelled.ChannelledSkill;
import server.skills.model.SkillTarget;
import server.socket.model.SocketResponse;
import server.socket.model.types.SkillMessageType;

@Slf4j
@Serdeable
@JsonTypeName("Blink")
@EqualsAndHashCode(callSuper = false)
public class Blink extends ChannelledSkill {

    public Blink() {
        super(
                "Blink",
                "Blinks your character forward",
                Map.of(),
                3000,
                50,
                false,
                false,
                1000,
                0,
                Map.of(ClassTypes.MAGE.getType(), 5),
                0,
                0);
    }

    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {
        Motion actorMotion = skillDependencies.getActorMotion();
        Location prevLocation = new Location(actorMotion);
        // validate that target location is within range.

        Location location = skillTarget.getLocation();
        actorMotion.setX(location.getX());
        actorMotion.setY(location.getY());
        actorMotion.setZ(location.getZ());

        // this is used to reference where to draw initial blink from
        skillTarget.setLocation(prevLocation);

//        double yawRadians = Math.toRadians(actorMotion.getYaw());
//
//        // Calculate the new coordinates
//        actorMotion.setX(actorMotion.getX() + (int) (getMaxRange() * Math.cos(yawRadians)));
//        actorMotion.setZ(actorMotion.getZ() + (int) (getMaxRange() * Math.sin(yawRadians)));
//

        Single.fromCallable(() -> {
            requestUpdateActorMotion(combatData.getActorId(), actorMotion);
            return true;
        }).delaySubscription(20, TimeUnit.MILLISECONDS).subscribe();

    }

}
