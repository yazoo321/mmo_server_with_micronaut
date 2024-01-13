package server.skills.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import server.attribute.stats.service.StatsService;
import server.combat.model.CombatData;
import server.session.SessionParamHelper;

import java.util.Map;
import java.util.Random;

@RequiredArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "name")
public abstract class Skill {


    private final String name;

    private final String description;

    private final Map<String, Double> derived;

    private final Integer maxRange;

    private final Map<String, Integer> requirements;

    @Inject
    protected SessionParamHelper sessionParamHelper;

    @Inject
    protected StatsService statsService;

    protected Random rand = new Random();

    public abstract void startSkill(CombatData combatData, SkillTarget skillTarget);

    public abstract void endSkill(CombatData combatData, SkillTarget skillTarget);

    public abstract boolean canApply(CombatData combatData, SkillTarget skillTarget);
}
