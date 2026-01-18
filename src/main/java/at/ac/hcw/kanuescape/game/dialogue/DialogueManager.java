package at.ac.hcw.kanuescape.game.dialogue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DialogueManager {

    // Speichert pro Object, wie oft schon geklickt wurde
    private final Map<String, Integer> clickCountsByType = new HashMap<>();

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
