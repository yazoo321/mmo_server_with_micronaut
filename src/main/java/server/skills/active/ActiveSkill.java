package server.skills.active;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonProperty;
import server.attribute.stats.model.Stats;
import server.combat.model.CombatData;
import server.common.dto.Motion;
import server.skills.behavior.InstantSkill;
import server.skills.behavior.TravelSkill;
import server.skills.model.Skill;
import server.skills.model.SkillTarget;

public abstract class ActiveSkill extends Skill implements InstantSkill, TravelSkill {

    @JsonProperty
    protected Integer durationMs;

    @JsonProperty
    protected Integer ticks;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public ActiveSkill(
            String name,
            String description,
            Map<String, Double> derived,
            Integer cooldown,
            Integer maxRange,
            Integer travelSpeed,
            Map<String, Integer> requirements,
            int durationMs,
            int ticks) {
        super(name, description, derived, maxRange, requirements, cooldown, travelSpeed);
        this.durationMs = durationMs;
        this.ticks = ticks;
    }

    @Override
    public boolean canApply(CombatData combatData, SkillTarget skillTarget) {
        Map<String, Instant> skillsOnCd = combatData.getActivatedSkills();

        if (skillsOnCd.containsKey(this.getName())) {
            if (skillsOnCd
                    .get(this.getName())
                    .plusMillis(this.getCooldown())
                    .isBefore(Instant.now())) {
                skillsOnCd.remove(this.getName());

                return true;
            }

            return false;
        } else {
            return true;
        }
    }

    @Override
    public void instantEffect(CombatData combatData, SkillTarget skillTarget) {
        this.endSkill(combatData, skillTarget);
    }

    @Override
    public void travel(CombatData combatData, SkillTarget skillTarget) {
        if (getTravelSpeed() == null || getTravelSpeed() == 0) {
            instantEffect(combatData, skillTarget);
            return;
        }

        String targetId = skillTarget.getTargetId();
        Motion targetMotion = actorMotionRepository.fetchActorMotion(targetId).blockingGet();
        Motion actorMotion =
                actorMotionRepository.fetchActorMotion(combatData.getActorId()).blockingGet();

        int x = targetMotion.getX() - actorMotion.getX();
        int y = targetMotion.getY() - actorMotion.getY();
        int z = targetMotion.getZ() - actorMotion.getZ();

        Double distance = Math.sqrt(x * x + y * y + z * z);

        double time = Math.floor(((distance / getTravelSpeed()) - 100));
        time = Math.max(Math.floor(time), 100);

        scheduler.schedule(
                () -> this.instantEffect(combatData, skillTarget),
                (long) time,
                TimeUnit.MILLISECONDS);
    }

    protected void checkDeath(Stats stats, String actorId) {
        combatService.handleActorDeath(stats, actorId);
    }
}
