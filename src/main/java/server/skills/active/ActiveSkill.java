package server.skills.active;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.websocket.WebSocketSession;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.status.model.ActorStatus;
import server.combat.model.CombatData;
import server.common.dto.Motion;
import server.skills.behavior.InstantSkill;
import server.skills.behavior.TravelSkill;
import server.skills.model.Skill;
import server.skills.model.SkillDependencies;
import server.skills.model.SkillTarget;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
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
    public Single<Boolean> canApply(CombatData combatData, SkillTarget skillTarget) {
        Map<String, Instant> skillsOnCd = combatData.getActivatedSkills();

        // check if skill is on CD
        if (skillsOnCd.containsKey(this.getName())) {
            if (skillsOnCd
                    .get(this.getName())
                    .plusMillis(this.getCooldown())
                    .isBefore(Instant.now())) {
                skillsOnCd.remove(this.getName());
            } else {
                return Single.just(false);
            }
        }
        // validate location
        return Single.just(true);
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

    @Override
    protected Single<Boolean> prepareApply(CombatData combatData, SkillTarget skillTarget, WebSocketSession session) {
        Single<Stats> actorStatsSingle = statsService.getStatsFor(combatData.getActorId());
        Single<Stats> targetStatsSingle = statsService.getStatsFor(skillTarget.getTargetId());
        Single<ActorStatus> actorStatusSingle = statusService.getActorStatus(combatData.getActorId());
        Single<ActorStatus> targetStatusSingle = statusService.getActorStatus(skillTarget.getTargetId());

        return Single.zip(actorStatsSingle, targetStatsSingle, actorStatusSingle, targetStatusSingle,
                (actorStats, targetStats, actorStatus, targetStatus) -> {
                    if (actorStatus.isDead() || targetStatus.isDead()) {
                        log.info("Caster or target is dead, skipping casting of elcipse burst");
                        return false;
                    }
                    if (!actorStatus.canCast()) {
                        log.info("Skipping cast fireball as actor cannot cast at this time");
                        return false;
                    }

                    this.session = session;
                    this.skillDependencies = SkillDependencies.builder()
                            .actorStats(actorStats)
                            .targetStats(targetStats)
                            .actorStatus(actorStatus)
                            .targetStatus(targetStatus)
                            .combatData(combatData)
                            .skillTarget(skillTarget)
                            .build();

                    activateSkillInCache(combatData);

                    return true;
                });
    }

    private void activateSkillInCache(CombatData combatData) {
        Map<String, Instant> activatedSkills = combatData.getActivatedSkills();
        activatedSkills.put(this.getName(), Instant.now());
        combatDataCache.cacheCombatData(combatData.getActorId(), combatData);
    }

}
