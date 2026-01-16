package at.ac.hcw.kanuescape.game.dialogue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class dialogueManager {

    private final Map<String, Integer> clickCountsByType = new HashMap<>();

    /** liefert passenden Text UND zählt intern hoch */
    public String nextTextForType(String type) {
        if (type == null || type.isBlank()) type = "unknown";

        int count = clickCountsByType.getOrDefault(type, 0) + 1; // startet bei 1
        clickCountsByType.put(type, count);

        List<String> variants = dialogueTexts.VARIANTS.get(type);

        // 1..3: Varianten
        if (variants != null && count <= variants.size()) {
            return variants.get(count - 1);
        }

        // 10+: anderer Default
        if (count >= 10) {
            return dialogueTexts.GENERIC_AFTER_TEN;
        }

        // 4..9 oder unknown: normaler Default
        return dialogueTexts.GENERIC_AFTER_VARIANTS;
    }
}
