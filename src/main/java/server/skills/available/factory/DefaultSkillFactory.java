package server.skills.available.factory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import server.attribute.stats.service.StatsService;
import server.combat.service.CombatService;
import server.motion.repository.ActorMotionRepository;
import server.session.SessionParamHelper;
import server.skills.available.destruction.fire.Fireball;
import server.skills.available.restoration.heals.BasicHeal;
import server.skills.model.Skill;
import server.socket.service.ClientUpdatesService;

@Singleton
public class DefaultSkillFactory implements SkillFactory {

    Map<String, Class<? extends Skill>> skillTypes = new HashMap<>();

    //    @Inject SocketResponseSubscriber socketResponseSubscriber;
    @Inject ClientUpdatesService clientUpdatesService;

    @Inject SessionParamHelper sessionParamHelper;

    @Inject StatsService statsService;

    @Inject CombatService combatService;

    @Inject ActorMotionRepository actorMotionRepository;

    public DefaultSkillFactory() {
        skillTypes.put("fireball", Fireball.class);
        skillTypes.put("basic heal", BasicHeal.class);
    }

    @Override
    public Skill createSkill(String skillType) {
        try {
            Class<? extends Skill> skillClass = skillTypes.get(skillType);
            Skill skill = skillClass.getDeclaredConstructor().newInstance();
            skill.setClientUpdatesService(clientUpdatesService);
            //            skill.setSocketResponseSubscriber(socketResponseSubscriber);
            skill.setStatsService(statsService);
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
