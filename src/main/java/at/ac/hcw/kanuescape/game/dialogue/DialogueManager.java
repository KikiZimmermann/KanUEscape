package at.ac.hcw.kanuescape.game.dialogue;

import at.ac.hcw.kanuescape.game.KochManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DialogueManager {
    private final KochManager koch;

    // Speichert pro Object, wie oft schon geklickt wurde
    private final Map<String, Integer> clickCountsByType = new HashMap<>();

    // 1) gid -> type
    private final Map<Integer, String> gidToType = new HashMap<>();

    // Types, die loopen sollen:
    private static final Set<String> LOOP_TYPES = Set.of("picture");
    private final Map<String, Integer> lineIndexByType = new HashMap<>();

    public DialogueManager(KochManager koch) {

        this.koch = koch;

        register("bed", 85, 86, 97, 98, 109, 110);
        register("picture_frame", 62);
        register("window", 11, 23);
        register("coffee_maker", 76);
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
        register("bookcase", 81, 82, 93, 94);
        register("sink", 64);
        register("cutting_board", 63);
        register("kitchen_counter", 58, 70);
        register("stove", 71, 59);
        register("cabinet", 55, 56);
        register("fridge", 72);
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

        if (koch != null) {
            switch (type) {
                case "fridge": return koch.fridge();
                case "sink": return koch.water();            // sink = wasserhahn bei dir
                case "cutting_board": return koch.board();
                case "stove": return koch.stove();
                case "kitchen_counter": return koch.shelf(); // oder cabinet? je nachdem was 58/70 ist
                case "cabinet": return koch.cabinet();
            }
        }

        List<String> variants = DialogueTexts.VARIANTS.get(type);

        // Sonderfall: picture = fortlaufende Zeilen (1 Klick = 1 Zeile)
        if ("picture".equals(type) && variants != null && !variants.isEmpty()) {
            int idx = lineIndexByType.getOrDefault(type, 0);
            String line = variants.get(idx);

            // idx erhöhen + loopen
            idx = (idx + 1) % variants.size();
            lineIndexByType.put(type, idx);

            return line;
        }

        int count = clickCountsByType.getOrDefault(type, 0) + 1;      //startet bei 1
        clickCountsByType.put(type, count);

        // Wenn es Varianten gibt:
        if (variants != null && !variants.isEmpty()) {

//

            // Normales Verhalten: zuerst Varianten 1..n
            if (count <= variants.size()) {
                return variants.get(count - 1);
            }
        }

        // Varianten 10+
        if (count >= 10) {
            return DialogueTexts.GENERIC_AFTER_TEN;
        }

        // Varianten 4 - 9
        return DialogueTexts.GENERIC_AFTER_VARIANTS;
    }

    public String typeForGid(int gid) {
        return gidToType.get(gid);
    }


    // Specifically Picture
    public String nextPictureLine() {
        List<String> lines = DialogueTexts.VARIANTS.get("picture");
        if (lines == null || lines.isEmpty()) return "";

        int idx = lineIndexByType.getOrDefault("picture", 0);
        if (idx >= lines.size()) idx = 0; // safety

        String line = lines.get(idx);
        lineIndexByType.put("picture", idx + 1); // wichtig: +1, NICHT modulo

        return line;
    }

    public boolean isPictureFinished() {
        List<String> lines = DialogueTexts.VARIANTS.get("picture");
        if (lines == null) return true;

        int idx = lineIndexByType.getOrDefault("picture", 0);
        return idx >= lines.size();
    }

    public void resetPicture() {
        lineIndexByType.put("picture", 0);
    }
}
