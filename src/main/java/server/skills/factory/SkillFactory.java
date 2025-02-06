package server.skills.factory;

import server.skills.model.Skill;

public interface SkillFactory {

    Skill createSkill(String skillType);
}
