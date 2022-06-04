package server.player.appearance.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class CreateCharacterDto {


    List<String> raceOptions;
    List<String> genderOptions;
    List<String> skinColorOptions;
    List<String> clothOptions;
    List<String> hairOptions;
    List<String> facialOptions;
}
