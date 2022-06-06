package server.player.appearance.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CreateCharacterGenericReq {

    CharacterAppearance characterAppearance;
    List<String> hairStyleOptions;
    List<String> hairColorOptions;
    List<String> skinColorOptions;
    List<AppearancePiece> appearancePieceList;
}