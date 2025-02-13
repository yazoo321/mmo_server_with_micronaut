package server.skills.active;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.websocket.WebSocketSession;
import io.reactivex.rxjava3.core.Single;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.DamageSource;
import server.attribute.stats.model.Stats;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.combat.model.CombatData;
import server.common.dto.Motion;
import server.items.equippable.model.EquippedItems;
import server.motion.model.MotionMessage;
import server.skills.behavior.InstantSkill;
import server.skills.behavior.TravelSkill;
import server.skills.model.Skill;
import server.skills.model.SkillTarget;

@Slf4j
public abstract class ActiveSkill extends Skill implements InstantSkill, TravelSkill {

    @JsonProperty protected Integer durationMs;

    @JsonProperty protected Integer ticks;

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
    public Single<Boolean> canApply() {
        CombatData combatData = skillDependencies.getCombatData();
        SkillTarget skillTarget = skillDependencies.getSkillTarget();
        Map<String, Instant> skillsOnCd = combatData.getActivatedSkills();
        WebSocketSession session = skillDependencies.getSession();

        // check if any skill was cast in the last 500ms (global cd)
        Instant max = skillsOnCd.values().stream().max(Instant::compareTo).orElse(null);

        if (max != null && max.plusMillis(500).isAfter(Instant.now())) {
            return Single.just(false);
        }

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

        Motion actorMotion =
                actorMotionRepository.fetchActorMotion(combatData.getActorId()).blockingGet();
        Motion targetMotion =
                actorMotionRepository.fetchActorMotion(skillTarget.getTargetId()).blockingGet();

        getSkillDependencies().setActorMotion(actorMotion);
        getSkillDependencies().setTargetMotion(targetMotion);

        return Single.just(true);
        //        Single<Motion> actorMotion =
        //                actorMotionRepository.fetchActorMotion(combatData.getActorId())
        //                        .doOnSuccess(motion -> log.info("Fetched actor motion: {}",
        // motion))
        //                        .doOnError(err -> log.error("Error fetching actor motion: {}",
        // err.getMessage()));
        //
        //        Single<Motion> targetMotion =
        //                actorMotionRepository.fetchActorMotion(skillTarget.getTargetId())
        //                        .doOnSuccess(motion -> log.info("Fetched target motion: {}",
        // motion))
        //                        .doOnError(err -> log.error("Error fetching target motion: {}",
        // err.getMessage()));
        //
        //        // consider pre-fetching other data too here, like status if required
        //        return Single.zip(
        //                actorMotion,
        //                targetMotion,
        //                (actor, target) -> {
        //                    getSkillDependencies().setActorMotion(actor);
        //                    getSkillDependencies().setTargetMotion(target);
        //
        //                    return true;
        ////                    return combatService.validatePositionLocation(
        ////                            combatData, actor, target, this.getMaxRange(), session);
        //                });
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

        Motion targetMotion = skillDependencies.getTargetMotion();
        Motion actorMotion = skillDependencies.getActorMotion();

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

    // TODO: several of these requests are now not required as we use producer to request instead.
    // e.g. stats services not required
    // do we need to consider re-design ?
    @Override
    protected Single<Boolean> prepareApply() {
        // Actor and Target motion have been added by canApply function.
        CombatData combatData = skillDependencies.getCombatData();
        SkillTarget skillTarget = skillDependencies.getSkillTarget();
        Single<Stats> actorStatsSingle = statsService.getStatsFor(combatData.getActorId());
        Single<Stats> targetStatsSingle = statsService.getStatsFor(skillTarget.getTargetId());
        Single<ActorStatus> actorStatusSingle =
                statusService.getActorStatus(combatData.getActorId());
        Single<ActorStatus> targetStatusSingle =
                statusService.getActorStatus(skillTarget.getTargetId());

        Single<Map<String, EquippedItems>> equipItemsSingle =
                equipItemService.getEquippedItemsMap(combatData.getActorId());
        // TODO: should we remove items from the request? the item effects for weapons now merged in
        // stats.
        return Single.zip(
                actorStatsSingle,
                targetStatsSingle,
                actorStatusSingle,
                targetStatusSingle,
                equipItemsSingle,
                (actorStats, targetStats, actorStatus, targetStatus, equippedItems) -> {
                    if (actorStatus.isDead() || targetStatus.isDead()) {
                        log.info("Caster or target is dead, skipping casting of eclipse burst");
                        return false;
                    }
                    if (!actorStatus.canCast()) {
                        log.info("Skipping cast fireball as actor cannot cast at this time");
                        return false;
                    }

                    this.getSkillDependencies().setActorStats(actorStats);
                    this.skillDependencies.setTargetStats(targetStats);
                    this.skillDependencies.setActorStatus(actorStatus);
                    this.skillDependencies.setTargetStatus(targetStatus);
                    this.skillDependencies.setCombatData(combatData);
                    this.skillDependencies.setEquippedItems(equippedItems);

                    activateSkillInCache(combatData);

                    return true;
                });
    }

    private void activateSkillInCache(CombatData combatData) {
        Map<String, Instant> activatedSkills = combatData.getActivatedSkills();
        activatedSkills.put(this.getName(), Instant.now());
        combatDataCache.cacheCombatData(combatData.getActorId(), combatData);
    }

    protected void requestTakeDamage(
            String sourceActor, String actorId, Map<String, Double> damageMap) {
        DamageSource damageSource =
                DamageSource.builder()
                        .actorId(actorId)
                        .sourceSkillId(this.getName())
                        .sourceActorId(sourceActor)
                        .damageMap(damageMap)
                        .build();
        skillProducer.requestTakeDamage(damageSource);
    }

    protected void requestUpdateActorMotion(String actorId, Motion actorMotion) {
        MotionMessage motionMessage = new MotionMessage();
        motionMessage.setActorId(actorId);
        motionMessage.setMotion(actorMotion);
        motionMessage.setUpdate(true); // not required
        skillProducer.requestUpdateMotion(motionMessage);
    }

    protected void requestAddStatusEffect(String target, Set<Status> statuses) {
        ActorStatus actorStatus = new ActorStatus();
        actorStatus.setActorId(target);
        actorStatus.setAdd(true);
        actorStatus.setActorStatuses(statuses);
        skillProducer.requestAddStatusToActor(actorStatus);
    }
}
