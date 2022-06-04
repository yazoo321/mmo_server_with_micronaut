package server.player.appearance.utils.human.female;

import server.player.appearance.model.MeshMaterialPair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HumanFemaleUtils {

    public static Map<String, MeshMaterialPair> getDefaultFemaleOptions() {
        Map<String, MeshMaterialPair> values = new HashMap<>();

        values.put("head", new MeshMaterialPair("SK_Hu_F_Head", "MI_HuF_Body_01"));
        values.put("ears", new MeshMaterialPair("SK_Hu_F_Head_Ears", "MI_HuF_Body_01"));
        values.put("boots", new MeshMaterialPair("SK_Hu_F_Boots", "MI_HuF_Body_01"));
        values.put("chest", new MeshMaterialPair("SK_Hu_F_Chest", "MI_HuF_Body_01"));
        values.put("bracers", new MeshMaterialPair("SK_Hu_F_Bracers", "MI_HuF_Body_01"));
        values.put("feet", new MeshMaterialPair("SK_Hu_F_Feet", "MI_HuF_Body_01"));
        values.put("hands", new MeshMaterialPair("SK_Hu_F_Hands", "MI_HuF_Body_01"));
        values.put("pants", new MeshMaterialPair("SK_Hu_F_Pants", "MI_HuF_Body_01"));

        return values;
    }

    public static List<String> getAvailableFemaleSkinColors() {
        return List.of("MI_HuF_Body_01", "MI_HuF_Body_02", "MI_HuF_Body_03", "MI_HuF_Body_04", "MI_HuF_Body_05");
    }

}
