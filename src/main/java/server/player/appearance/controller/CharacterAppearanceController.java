package server.player.appearance.controller;

import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import server.player.appearance.model.AppearancePiece;
import server.player.appearance.model.CharacterAppearance;
import server.player.appearance.model.CreateCharacterGenericReq;
import server.player.appearance.service.AppearanceService;

import javax.inject.Inject;
import java.util.List;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/v1/appearance")
public class CharacterAppearanceController {

    @Inject
    AppearanceService appearanceService;

    @Get("/default")
    public CreateCharacterGenericReq getDefaultOpts(@QueryValue String race, @QueryValue Boolean isMale) {
        return appearanceService.getDefaultAppearance(race, isMale);
    }

    @Post("/insert")
    public List<AppearancePiece> addPiecesToRepo(@Body CreateCharacterGenericReq req)  {
        return appearanceService.insertAppearancePieces(req.getAppearancePieceList());
    }
}
