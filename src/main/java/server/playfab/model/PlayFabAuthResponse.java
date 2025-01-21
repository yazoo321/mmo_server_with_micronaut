package server.playfab.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PlayFabAuthResponse {

    @JsonProperty("playfab_id")
    public String playFabId;

    public PlayFabAuthResponse(String playFabId) {
        this.playFabId = playFabId;
    }
}
