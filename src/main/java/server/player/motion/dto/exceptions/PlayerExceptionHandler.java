package server.player.motion.dto.exceptions;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import server.common.dto.ErrorResponseDto;

@Produces
@Singleton
@Requires(classes = {PlayerMotionException.class, ExceptionHandler.class})
public class PlayerExceptionHandler
        implements ExceptionHandler<PlayerMotionException, HttpResponse<?>> {

    @Override
    public HttpResponse<?> handle(HttpRequest request, PlayerMotionException exception) {
        return HttpResponse.badRequest(new ErrorResponseDto(exception.getMessage()));
    }
}
