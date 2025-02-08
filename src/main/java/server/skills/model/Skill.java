package server.skills.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.websocket.WebSocketSession;
import io.reactivex.rxjava3.core.Single;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.service.StatsService;
import server.attribute.status.service.StatusService;
import server.combat.model.CombatData;
import server.combat.model.CombatRequest;
import server.combat.repository.CombatDataCache;
import server.combat.service.CombatService;
import server.items.equippable.service.EquipItemService;
import server.motion.repository.ActorMotionRepository;
import server.session.SessionParamHelper;
import server.skills.available.cleric.heals.BasicHeal;
import server.skills.available.cleric.heals.HealingRain;
import server.skills.available.fighter.HeavyStrike;
import server.skills.available.fighter.Maim;
import server.skills.available.fighter.Rupture;
import server.skills.available.mage.arcane.Blink;
import server.skills.available.mage.fire.Fireball;
import server.skills.available.mage.nature.EclipseBurst;
import server.skills.available.mage.nature.MoonsVengeance;
import server.skills.available.mage.nature.SunSmite;
import server.skills.available.mage.nature.VineGrab;
import server.skills.producer.SkillProducer;
import server.socket.model.SocketResponse;
import server.socket.model.types.SkillMessageType;
import server.socket.service.WebsocketClientUpdatesService;

import java.util.Map;
import java.util.Random;

@Slf4j
@Serdeable
@ReflectiveAccess
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "name")
@JsonSubTypes({
        // cleric spells
        @JsonSubTypes.Type(value = BasicHeal.class, name = "Basic heal"),
        @JsonSubTypes.Type(value = HealingRain.class, name = "Healing rain"),

        // fighter spells
        @JsonSubTypes.Type(value = HeavyStrike.class, name = "Heavy strike"),
        @JsonSubTypes.Type(value = Maim.class, name = "Maim"),
        @JsonSubTypes.Type(value = Rupture.class, name = "Rupture"),

        // mage: arcane
        @JsonSubTypes.Type(value = Blink.class, name = "Blink"),

        // mage: fire
        @JsonSubTypes.Type(value = Fireball.class, name = "Fireball"),

        // mage: nature
        @JsonSubTypes.Type(value = EclipseBurst.class, name = "Eclipse burst"),
        @JsonSubTypes.Type(value = MoonsVengeance.class, name = "Moons vengeance"),
        @JsonSubTypes.Type(value = SunSmite.class, name = "Sun smite"),
        @JsonSubTypes.Type(value = VineGrab.class, name = "Vine grab"),
})
public abstract class Skill {

    @Getter @JsonProperty private String name;

    @Getter @JsonProperty private String description;

    @Getter @JsonProperty protected Map<String, Double> derived;

    @Getter @JsonProperty private Integer maxRange;

    @Getter @JsonProperty private Map<String, Integer> requirements;

    @Getter @JsonProperty private Integer cooldown;

    @Getter @JsonProperty private Integer travelSpeed;

    protected WebSocketSession session;

    // populated via factory methods
    @Setter protected WebsocketClientUpdatesService clientUpdatesService;
    @Setter protected SessionParamHelper sessionParamHelper;
    @Setter protected ActorMotionRepository actorMotionRepository;
    @Setter protected StatsService statsService;
    @Setter protected StatusService statusService;
    @Setter protected CombatService combatService;
    @Setter protected CombatDataCache combatDataCache;
    @Setter protected EquipItemService equipItemService;

    @Setter protected SkillProducer skillProducer;

    protected SkillDependencies skillDependencies;

    public SkillDependencies getSkillDependencies() {
        if (skillDependencies == null) {
            skillDependencies = new SkillDependencies();
        }
        return skillDependencies;
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

    public abstract void startSkill();

    public abstract void endSkill(CombatData combatData, SkillTarget skillTarget);

    public abstract Single<Boolean> canApply();

    protected abstract Single<Boolean> prepareApply();

    public void tryApply(CombatData combatData, SkillTarget skillTarget, WebSocketSession session) {
        getSkillDependencies().setSession(session);
        skillDependencies.setSkillTarget(skillTarget);
        skillDependencies.setCombatData(combatData);

        canApply()
                .doOnSuccess(
                        canApply -> {
                            if (!canApply) {
                                log.info("Cannot apply skill at this time");
                            }

                            prepareApply()
                                    .doOnSuccess(
                                            canStart -> {
                                                if (canStart) {
                                                    startSkill();
                                                }
                                            })
                                    .subscribe();
                        })
                .subscribe();
    }

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

        clientUpdatesService.sendUpdateToListeningIncludingSelf(message, castor);
    }
}
