package server.items.model.exceptions;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import server.common.dto.ErrorResponseDto;
import server.player.character.inventory.model.exceptions.InventoryException;

import javax.inject.Singleton;

@Produces
@Singleton
@Requires(classes = {InventoryException.class, ExceptionHandler.class})
public class ItemExceptionHandler implements ExceptionHandler<InventoryException, HttpResponse<?>> {

    @Override
    public HttpResponse<?> handle(HttpRequest request, InventoryException exception) {
        return HttpResponse.badRequest(new ErrorResponseDto(exception.getMessage()));
    }
}
