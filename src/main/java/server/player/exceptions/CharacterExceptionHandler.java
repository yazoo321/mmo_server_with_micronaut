package server.player.exceptions;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import server.common.dto.ErrorResponseDto;

import jakarta.inject.Singleton;

@Produces
@Singleton
@Requires(classes = {CharacterException.class, ExceptionHandler.class})
public class CharacterExceptionHandler implements ExceptionHandler<CharacterException, HttpResponse<?>> {

    @Override
    public HttpResponse<?> handle(HttpRequest request, CharacterException exception) {
        return HttpResponse.badRequest(new ErrorResponseDto(exception.getMessage()));
    }
}
