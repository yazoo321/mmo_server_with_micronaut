package server.skills.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.websocket.WebSocketSession;
import java.util.Map;
import java.util.Random;
import lombok.NoArgsConstructor;
import lombok.Setter;
import server.attribute.stats.service.StatsService;
import server.combat.model.CombatData;
import server.combat.model.CombatRequest;
import server.combat.service.CombatService;
import server.motion.repository.ActorMotionRepository;
import server.session.SessionParamHelper;
import server.skills.available.destruction.fire.Fireball;
import server.skills.available.restoration.heals.BasicHeal;
import server.socket.model.SocketResponse;
import server.socket.model.types.SkillMessageType;
import server.socket.service.ClientUpdatesService;
import server.socket.service.WebsocketClientUpdatesService;

@Serdeable
@ReflectiveAccess
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "name")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Fireball.class, name = "Fireball"),
    @JsonSubTypes.Type(value = BasicHeal.class, name = "Basic heal"),
})
public abstract class Skill {

    @JsonProperty private String name;

    @JsonProperty private String description;

    @JsonProperty protected Map<String, Double> derived;

    @JsonProperty private Integer maxRange;

    @JsonProperty private Map<String, Integer> requirements;

    @JsonProperty private Integer cooldown;

    @JsonProperty private Integer travelSpeed;

    protected WebSocketSession session;

    // populated via factory methods
    @Setter protected WebsocketClientUpdatesService clientUpdatesService;
    @Setter protected SessionParamHelper sessionParamHelper;
    @Setter protected ActorMotionRepository actorMotionRepository;
    @Setter protected StatsService statsService;
    @Setter protected CombatService combatService;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Double> getDerived() {
        return derived;
    }

    public Integer getMaxRange() {
        return maxRange;
    }

    public Map<String, Integer> getRequirements() {
        return requirements;
    }

    public Integer getCooldown() {
        return cooldown;
    }

    public Integer getTravelSpeed() {
        return travelSpeed;
    }

    public Skill(
            String name,
            String description,
            Map<String, Double> derived,
            Integer maxRange,
            Map<String, Integer> requirements,
            Integer cooldown,
            Integer travelSpeed) {

        this.name = name;
        this.description = description;
        this.derived = derived;
        this.maxRange = maxRange;
        this.requirements = requirements;
        this.cooldown = cooldown;
        this.travelSpeed = travelSpeed;
    }

    protected Random rand = new Random();

    public abstract void startSkill(
            CombatData combatData, SkillTarget skillTarget, WebSocketSession session);

    public abstract void endSkill(CombatData combatData, SkillTarget skillTarget);

    public abstract boolean canApply(CombatData combatData, SkillTarget skillTarget);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Skill)) {
            return false;
        }

        return ((Skill) o).getName().equals(this.getName());
    }

    @Override
    public int hashCode() {
        int res = 17;
        if (getName() != null) {
            res = 31 * res + getName().hashCode();
        }
        return res;
    }

    protected void updateSessionInitiateSkill(String castor, SkillTarget skillTarget) {
        SocketResponse message = new SocketResponse();
        message.setMessageType(SkillMessageType.INITIATE_SKILL.getType());

        CombatRequest request = new CombatRequest();
        request.setSkillId(this.getName());
        request.setSkillTarget(skillTarget);
        request.setActorId(castor);

        message.setCombatRequest(request);

        clientUpdatesService.sendToSelf(session, message);
    }
}
