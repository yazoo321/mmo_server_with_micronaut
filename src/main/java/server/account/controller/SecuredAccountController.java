package server.account.controller;

import io.micronaut.context.annotation.Parameter;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import server.account.dto.Account;
import server.account.service.AccountService;

import javax.inject.Inject;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/account")
public class SecuredAccountController {

    @Inject
    AccountService accountService;

    @Get("/get-user")
    public Account getAccountByUsername(@Parameter String username) {
        return accountService.fetchAccount(username);
    }
}
