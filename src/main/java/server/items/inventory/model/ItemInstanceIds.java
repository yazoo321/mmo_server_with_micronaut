package server.items.inventory.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Serdeable
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude()
@Builder
public class ItemInstanceIds {

    String playerName;
    List<String> itemInstanceIds;
}
