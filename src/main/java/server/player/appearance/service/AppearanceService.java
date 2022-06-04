package server.player.appearance.service;

import lombok.extern.slf4j.Slf4j;
import server.player.appearance.service.human.HumanAppearanceService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class AppearanceService {

    @Inject
    HumanAppearanceService humanAppearanceService;

    public void getDefaultAppearance(String race, Boolean isMale, String clothes) {

        if (race.equalsIgnoreCase("human")) {
            humanAppearanceService.getDefaultMaleCharacterAppearanceValues(isMale, clothes);
        }
    }

}
