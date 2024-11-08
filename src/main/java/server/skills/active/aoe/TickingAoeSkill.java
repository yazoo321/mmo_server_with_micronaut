package server.skills.active.aoe;

import io.micronaut.websocket.WebSocketSession;
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
    public void applyEffectAtInterval(CombatData combatData) {

        if (!channelingInProgress(combatData)) {
            return;
        }

        int effectTimer = this.durationMs / ticks;
        scheduler.schedule(
                () -> {
                    if (channelingInProgress(combatData)) {
                        applyEffect(combatData);
                        applyEffectAtInterval(combatData);
                    }
                },
                effectTimer,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void startSkill(
            CombatData combatData, SkillTarget skillTarget, WebSocketSession session) {
        this.session = session;
        startChanneling(combatData, skillTarget);
        updateSessionInitiateSkill(combatData.getActorId(), skillTarget);
    }

    @Override
    public void startChanneling(CombatData combatData, SkillTarget skillTarget) {
        // slightly different behaviour to Channelling class
        notifyStartChannel(combatData.getActorId());
        combatData.setCombatState(CombatState.CHANNELING.getType());
        applyEffectAtInterval(combatData);

        // Schedule a task to periodically check the channeling status
        ScheduledFuture<?> channelingTask =
                scheduler.scheduleAtFixedRate(
                        () -> {
                            if (!channelingInProgress(combatData)) {
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
                    if (channelingInProgress(combatData)) {
                        notifyStopChannel(combatData.getActorId(), true);
                    }
                    combatData.setCombatState(CombatState.IDLE.getType());
                    channelingTask.cancel(true);
                },
                this.getCastTime(),
                TimeUnit.MILLISECONDS);
    }

}
