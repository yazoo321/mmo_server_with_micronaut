package server.skills.available.factory;

import server.skills.model.Skill;

public interface SkillFactory {

    Skill createSkill(String skillType);
}
