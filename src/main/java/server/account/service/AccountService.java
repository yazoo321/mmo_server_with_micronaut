package server.account.service;

import com.org.mmo_server.repository.model.tables.pojos.Users;
import org.springframework.transaction.annotation.Transactional;
import server.account.dto.Account;
import server.account.dto.RegisterDto;
import server.account.repository.AccountRepository;
import server.security.BCryptPasswordEncoderService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;

@Singleton
public class AccountService {

    @Inject
    AccountRepository accountRepository;

    @Inject
    BCryptPasswordEncoderService bCryptPasswordEncoderService;

    public Account fetchAccount(String username) {
        Users user = accountRepository.fetchByUsername(username);

        if (null == user) {
            return new Account();
        }
        return new Account(user.getUsername(), user.getEmail());
    }

    @Transactional
    public Account registerUser(RegisterDto registerDto) {
        try {
            String encodedPassword = bCryptPasswordEncoderService.encode(registerDto.getPassword());

            LocalDateTime now = LocalDateTime.now();
            Users user = new Users();
            user.setEmail(registerDto.getEmail());
            user.setUsername(registerDto.getUsername());
            // ensure password is encoded. Can be done on repo level instead.
            user.setPassword(encodedPassword);
            user.setEnabled(true);
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            user.setLastLoggedInAt(now);

            accountRepository.createUser(user);

            Account account = new Account();
            account.setEmail(user.getEmail());
            account.setUsername(user.getUsername());

            return account;
        } catch (Exception e) {
            // log exception in future
            return new Account();
        }

    }
}
