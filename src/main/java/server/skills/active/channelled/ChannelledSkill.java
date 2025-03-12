package server.skills.active.channelled;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import server.combat.model.CombatData;
import server.combat.model.CombatRequest;
import server.combat.model.CombatState;
import server.skills.active.ActiveSkill;
import server.skills.model.SkillTarget;
import server.socket.model.SocketResponse;
import server.socket.model.types.SkillMessageType;

public abstract class ChannelledSkill extends ActiveSkill {

    @Getter @JsonProperty protected final int castTime;
    @JsonProperty protected final boolean allowsMovement;
    @JsonProperty protected final boolean canInterrupt;

    public boolean getAllowsMovement() {
        return allowsMovement;
    }

    public boolean getCanInterrupt() {
        return canInterrupt;
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public ChannelledSkill(
            String name,
            String description,
            Map<String, Double> derived,
            int cooldown,
            int castTime,
            boolean allowsMovement,
            boolean canInterrupt,
            int maxRange,
            int travelSpeed,
            Map<String, Integer> requirements,
            int durationMs,
            int ticks) {
        super(
                name,
                description,
                derived,
                cooldown,
                maxRange,
                travelSpeed,
                requirements,
                durationMs,
                ticks);
        this.castTime = castTime;
        this.allowsMovement = allowsMovement;
        this.canInterrupt = canInterrupt;
    }

    @Override
    public void startSkill() {
        startChanneling();
    }

    public void interruptChannel(CombatData combatData) {
        // TBD
    }

    public void stopChannel(CombatData combatData) {
        // TBD
        notifyStopChannel(combatData.getActorId(), false);
    }

    public void startChanneling() {
        CombatData combatData = skillDependencies.getCombatData();
        SkillTarget skillTarget = skillDependencies.getSkillTarget();

        notifyStartChannel(combatData.getActorId());
        combatData.setCombatState(CombatState.CHANNELING.getType());

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
                        TimeUnit.MILLISECONDS); // Adjust the interval as needed

        // Schedule a task to execute the skill after the channel time
        scheduler.schedule(
                () -> {
                    if (channelingInProgress()) {
                        notifyStopChannel(combatData.getActorId(), true);
                        this.travel(combatData, skillTarget);
                        updateSessionInitiateSkill(combatData.getActorId(), skillTarget);

                    }
                    combatData.setCombatState(CombatState.IDLE.getType()); // Reset combat state
                    channelingTask.cancel(true); // Stop the periodic check
                },
                this.getCastTime(),
                TimeUnit.MILLISECONDS);
    }

    protected boolean channelingInProgress() {
        CombatData combatData = skillDependencies.getCombatData();
        return combatData.getCombatState().equalsIgnoreCase(CombatState.CHANNELING.getType());
    }

    protected void notifyStartChannel(String actorId) {
        SocketResponse message = new SocketResponse();
        message.setMessageType(SkillMessageType.START_CHANNELLING.getType());

        CombatRequest request = new CombatRequest();
        request.setSkillId(this.getName());
        request.setActorId(actorId);
        message.setCombatRequest(request);

        clientUpdatesService.sendUpdateToListeningIncludingSelf(message, actorId);
    }

    protected void notifyStopChannel(String actorId, boolean channelSuccess) {
        SocketResponse message = new SocketResponse();
        message.setMessageType(SkillMessageType.STOP_CHANNELLING.getType());
        CombatRequest combat = new CombatRequest();
        combat.setActorId(actorId);
        combat.setSkillId(getName());
        message.setCombatRequest(combat);

        clientUpdatesService.sendUpdateToListeningIncludingSelf(message, actorId);
    }
}
