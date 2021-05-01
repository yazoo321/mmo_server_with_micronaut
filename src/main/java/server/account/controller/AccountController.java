package server.account.controller;

import com.org.mmo_server.repository.model.tables.pojos.Users;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import server.account.dto.Account;
import server.account.dto.RegisterDto;
import server.account.service.AccountService;

import javax.inject.Inject;
import javax.validation.Valid;


@Secured(SecurityRule.IS_ANONYMOUS)
@Controller
public class AccountController {

    @Inject
    AccountService accountService;

    @Post("/register")
    public Account register(@Body RegisterDto user) {
        return accountService.registerUser(user);
    }
}
