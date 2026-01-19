package at.ac.hcw.kanuescape.game.dialogue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DialogueManager {

    // Speichert pro Object, wie oft schon geklickt wurde
    private final Map<String, Integer> clickCountsByType = new HashMap<>();

    // 1) gid -> type
    private final Map<Integer, String> gidToType = new HashMap<>();

    public DialogueManager() {

        register("bed", 85, 86, 97, 98, 109, 110);
        register("picture_frame", 62);
        register("window", 11, 23);
        register("coffee_maker", 76);
        register("sink", 64);
        register("cutting_board", 63);
        register("chair",  65, 53);
        register("coffee", 75);
        register("dog_food", 79);
        register("tv", 83, 84);
        register("picture", 66);
        register("lamp", 99, 100, 88);
        register("plant", 80, 92);
        register("clock", 67);
        register("telephone", 54);
        register("shelves", 112);
        register("couch", 104, 105, 106, 116, 117, 118);
        register("trash", 52);
        register("vase", 87);
//        register("exit", );
//        register("bathroom", );
    }

    private void register(String type, int... gids) {
        for (int gid : gids) {
            gidToType.put(gid, type);
        }
    }


    /**
     * liefert passenden Text zu Objekten UND zählt intern hoch
     */
    public String nextTextForType(String type) {
        if (type == null || type.isBlank()) type = "unknown";

        int count = clickCountsByType.getOrDefault(type, 0) + 1;      //startet bei 1
        clickCountsByType.put(type, count);

        List<String> variants = DialogueTexts.VARIANTS.get(type);

        // Varianten 1 - 3
        if (variants != null && count <= variants.size()) {
            return variants.get(count - 1);
        }

        // Varianten 10+
        if (count >= 10) {
            return DialogueTexts.GENERIC_AFTER_TEN;
        }

        // Varianten 4 - 9
        return DialogueTexts.GENERIC_AFTER_VARIANTS;
    }
}
