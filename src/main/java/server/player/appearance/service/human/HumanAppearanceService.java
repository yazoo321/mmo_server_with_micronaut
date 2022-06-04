package server.player.appearance.service.human;


import lombok.extern.slf4j.Slf4j;
import server.player.appearance.model.MeshMaterialPair;
import server.player.appearance.utils.human.female.HumanFemaleUtils;
import server.player.appearance.utils.human.male.HumanMaleUtils;

import javax.inject.Singleton;
import java.util.Map;

@Slf4j
@Singleton
public class HumanAppearanceService {

    // Some functions to assist with character creation
    public void getDefaultMaleCharacterAppearanceValues(Boolean isMale, String clothes) {
        if (isMale) {
            Map<String, MeshMaterialPair> properties = HumanMaleUtils.getDefaultMaleOptions();
        } else {
            HumanFemaleUtils.getDefaultFemaleOptions();
        }
    }
}
