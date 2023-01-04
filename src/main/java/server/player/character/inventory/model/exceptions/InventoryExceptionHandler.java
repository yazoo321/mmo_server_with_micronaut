package server.player.character.inventory.model.exceptions;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import server.common.dto.ErrorResponseDto;

@Produces
@Singleton
@Requires(classes = {InventoryException.class, ExceptionHandler.class})
public class InventoryExceptionHandler
        implements ExceptionHandler<InventoryException, HttpResponse<?>> {

    @Override
    public HttpResponse<?> handle(HttpRequest request, InventoryException exception) {
        return HttpResponse.badRequest(new ErrorResponseDto(exception.getMessage()));
    }
}
