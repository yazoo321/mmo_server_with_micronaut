package server.skills.behavior;

public interface PeriodicEffect {

    void applyEffectAtInterval();

    // apply effect will be called at the interval from above
    void applyEffect();
}
