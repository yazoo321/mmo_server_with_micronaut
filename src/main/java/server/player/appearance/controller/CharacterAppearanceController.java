package server.player.appearance.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import server.player.appearance.service.AppearanceService;

import javax.inject.Inject;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/v1/appearance")
public class CharacterAppearanceController {

    @Inject
    AppearanceService appearanceService;

    @Get("/default")
    public void getDefaultMale(@QueryValue String race, @QueryValue Boolean isMale, @QueryValue String clothes) {
        appearanceService.getDefaultAppearance(race, isMale, clothes);
    }
}
