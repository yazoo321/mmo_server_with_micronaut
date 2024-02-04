package server.skills.available.factory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import server.attribute.stats.service.StatsService;
import server.session.SessionParamHelper;
import server.skills.available.destruction.fire.Fireball;
import server.skills.available.restoration.heals.BasicHeal;
import server.skills.model.Skill;
import server.socket.model.SocketResponseSubscriber;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class DefaultSkillFactory implements SkillFactory {

    Map<String, Class<? extends Skill>> skillTypes = new HashMap<>();

    @Inject
    SocketResponseSubscriber socketResponseSubscriber;

    @Inject
    SessionParamHelper sessionParamHelper;

    @Inject
    StatsService statsService;

    public DefaultSkillFactory() {
        skillTypes.put("fireball", Fireball.class);
        skillTypes.put("basic heal", BasicHeal.class);
    }


    @Override
    public Skill createSkill(String skillType) {
        try {
            Class<? extends Skill> skillClass = skillTypes.get(skillType);
            Skill skill = skillClass.getDeclaredConstructor().newInstance();
            skill.setSocketResponseSubscriber(socketResponseSubscriber);
            skill.setStatsService(statsService);
            skill.setSessionParamHelper(sessionParamHelper);

            return skill;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Error creating skill instance", e);
        }
    }
}
