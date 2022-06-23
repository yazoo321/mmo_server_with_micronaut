package server.player.character.equippable.model.exceptions;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import server.common.dto.ErrorResponseDto;

import javax.inject.Singleton;

@Produces
@Singleton
@Requires(classes = {EquipException.class, ExceptionHandler.class})
public class EquipExceptionHandler implements ExceptionHandler<EquipException, HttpResponse<?>> {

    @Override
    public HttpResponse<?> handle(HttpRequest request, EquipException exception) {
        return HttpResponse.badRequest(new ErrorResponseDto(exception.getMessage()));
    }
}
