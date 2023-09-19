package server.items.inventory.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude()
@Builder
public class ItemInstanceIds {

    String playerName;
    List<String> itemInstanceIds;
}
