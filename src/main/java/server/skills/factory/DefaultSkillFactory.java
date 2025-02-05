package server.skills.factory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import server.attribute.stats.service.StatsService;
import server.attribute.status.service.StatusService;
import server.combat.service.CombatService;
import server.motion.repository.ActorMotionRepository;
import server.session.SessionParamHelper;
import server.skills.available.cleric.heals.BasicHeal;
import server.skills.available.cleric.heals.HealingRain;
import server.skills.available.mage.fire.Fireball;
import server.skills.available.mage.nature.EclipseBurst;
import server.skills.available.mage.nature.MoonsVengeance;
import server.skills.available.mage.nature.SunSmite;
import server.skills.available.mage.nature.VineGrab;
import server.skills.model.Skill;
import server.socket.service.WebsocketClientUpdatesService;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class DefaultSkillFactory implements SkillFactory {

    Map<String, Class<? extends Skill>> skillTypes = new HashMap<>();

    @Inject
    WebsocketClientUpdatesService clientUpdatesService;

    @Inject SessionParamHelper sessionParamHelper;

    @Inject StatsService statsService;

    @Inject
    StatusService statusService;

    @Inject CombatService combatService;

    @Inject ActorMotionRepository actorMotionRepository;

    public DefaultSkillFactory() {
        skillTypes.put("fireball", Fireball.class);
        skillTypes.put("basic heal", BasicHeal.class);
        skillTypes.put("healing rain", HealingRain.class);
        skillTypes.put("vine grab", VineGrab.class);

        skillTypes.put("eclipse burst", EclipseBurst.class);
        skillTypes.put("moons vengeance", MoonsVengeance.class);
        skillTypes.put("sun smite", SunSmite.class);
    }

    @Override
    public Skill createSkill(String skillType) {
        try {
            Class<? extends Skill> skillClass = skillTypes.get(skillType);
            Skill skill = skillClass.getDeclaredConstructor().newInstance();
            skill.setClientUpdatesService(clientUpdatesService);
            skill.setStatsService(statsService);
            skill.setStatusService(statusService);
            skill.setSessionParamHelper(sessionParamHelper);
            skill.setCombatService(combatService);
            skill.setActorMotionRepository(actorMotionRepository);

            return skill;
        } catch (InstantiationException
                | IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            throw new RuntimeException("Error creating skill instance", e);
        }
    }
}
