package server.security;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.*;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import server.account.repository.AccountRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthenticationProviderUserPassword implements AuthenticationProvider {

    @Inject
    AccountRepository accountRepository;

    @Override
    public Publisher<AuthenticationResponse> authenticate(@Nullable HttpRequest<?> httpRequest, AuthenticationRequest<?, ?> authenticationRequest) {
        return Flowable.create(emitter -> {

            String username = (String) authenticationRequest.getIdentity();
            String pw = (String) authenticationRequest.getSecret();
            // consider sanitisation
            boolean validCredentials = accountRepository.validCredentials(username, pw);

            if (validCredentials) {
                emitter.onNext(new UserDetails(username, accountRepository.getRolesForUser(username)));
                emitter.onComplete();
            } else {
                emitter.onError(new AuthenticationException(new AuthenticationFailed()));
            }
        }, BackpressureStrategy.ERROR);
    }
}
