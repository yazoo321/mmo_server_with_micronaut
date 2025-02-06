package server.skills.active.aoe;

import server.combat.model.CombatData;
import server.combat.model.CombatState;
import server.skills.behavior.PeriodicEffect;
import server.skills.model.SkillTarget;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class TickingAoeSkill extends AbstractAoeSkill implements PeriodicEffect {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public TickingAoeSkill(String name, String description, Map<String, Double> derived, int cooldown, int castTime,
                           boolean allowsMovement, boolean canInterrupt, int maxRange, int travelSpeed,
                           Map<String, Integer> requirements, int diameter, int durationMs, int ticks,
                           boolean includeCaster) {
        super(name, description, derived, cooldown, castTime, allowsMovement, canInterrupt, maxRange,
                travelSpeed, requirements, diameter, durationMs, ticks, includeCaster);
    }

    @Override
    public void applyEffectAtInterval() {
        if (!channelingInProgress()) {
            return;
        }

        int effectTimer = this.durationMs / ticks;
        scheduler.schedule(
                () -> {
                    if (channelingInProgress()) {
                        prepareApply()
                                .doOnSuccess(success -> {
                                    if (success) {
                                        applyEffect();
                                        applyEffectAtInterval();
                                    }
                                })
                                .subscribe();
                    }
                },
                effectTimer,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void startSkill() {
        startChanneling();
        CombatData combatData = skillDependencies.getCombatData();
        SkillTarget skillTarget = skillDependencies.getSkillTarget();
        updateSessionInitiateSkill(combatData.getActorId(), skillTarget);
    }

    @Override
    public void startChanneling() {
        CombatData combatData = skillDependencies.getCombatData();

        // slightly different behaviour to Channelling class
        notifyStartChannel(combatData.getActorId());
        combatData.setCombatState(CombatState.CHANNELING.getType());
        applyEffectAtInterval();

        // Schedule a task to periodically check the channeling status
        ScheduledFuture<?> channelingTask =
                scheduler.scheduleAtFixedRate(
                        () -> {
                            if (!channelingInProgress()) {
                                // Channeling interrupted or completed
                                scheduler.shutdownNow();
                            }
                        },
                        0,
                        100,
                        TimeUnit.MILLISECONDS);

        // Schedule a task to execute the skill after the channel time
        scheduler.schedule(
                () -> {
                    if (channelingInProgress()) {
                        notifyStopChannel(combatData.getActorId(), true);
                    }
                    combatData.setCombatState(CombatState.IDLE.getType());
                    channelingTask.cancel(true);
                },
                this.getCastTime(),
                TimeUnit.MILLISECONDS);
    }

}
