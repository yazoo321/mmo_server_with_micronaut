package server.player.appearance.utils.human.male;


import server.player.appearance.model.MeshMaterialPair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HumanMaleUtils {

    public static Map<String, MeshMaterialPair> getDefaultMaleOptions() {
        Map<String, MeshMaterialPair> values = new HashMap<>();

        values.put("head", new MeshMaterialPair("SK_Hu_M_Head", "MI_HuM_Body_01"));
        values.put("ears", new MeshMaterialPair("SK_Hu_M_Head_Ears", "MI_HuM_Body_01"));
        values.put("boots", new MeshMaterialPair("SK_Hu_M_Boots", "MI_HuM_Body_01"));
        values.put("chest", new MeshMaterialPair("SK_Hu_M_Chest", "MI_HuM_Body_01"));
        values.put("bracers", new MeshMaterialPair("SK_Hu_M_Bracers", "MI_HuM_Body_01"));
        values.put("feet", new MeshMaterialPair("SK_Hu_M_Feet", "MI_HuM_Body_01"));
        values.put("hands", new MeshMaterialPair("SK_Hu_M_Hands", "MI_HuM_Body_01"));
        values.put("pants", new MeshMaterialPair("SK_Hu_M_Pants", "MI_HuM_Body_01"));

        return values;
    }

    public static List<String> getAvailableMaleSkinColors() {
        return List.of("MI_HuM_Body_01", "MI_HuM_Body_02", "MI_HuM_Body_03", "MI_HuM_Body_04", "MI_HuM_Body_05");
    }


}
