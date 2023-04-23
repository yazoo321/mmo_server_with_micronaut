package server.monster.server_integration.model.exceptions;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import server.common.dto.ErrorResponseDto;

@Produces
@Singleton
@Requires(classes = {MobInstanceException.class, ExceptionHandler.class})
public class MobExceptionHandler implements ExceptionHandler<MobInstanceException, HttpResponse<?>> {

    @Override
    public HttpResponse<?> handle(HttpRequest request, MobInstanceException exception) {
        return HttpResponse.badRequest(new ErrorResponseDto(exception.getMessage()));
    }
}
