package at.ac.hcw.kanuescape.game.dialogue;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class DialogueTexts {

    private DialogueTexts() {}

    // Start Text
    private static final String[] INTRO_TEXT = {
            "07:32 AM.",
            "You have to get to university.",
            "Like... now.",
            "Unfortunately, you're still standing in your apartment.",
            "Tired. Slightly stressed.",
            "And with the strong feeling you've forgotten something important.",
            "Finish everything that needs to be done.",
            "Then you're allowed to go."
    };

    // End Text
    private static final String[] END_TEXT = {
            "You open the door.",
            "Everything is done.",
            "Nothing is beeping.",
            "Nothing is blinking.",
            "Nothing is screaming: YOU FORGOT SOMETHING!",
            "Keys: check.",
            "Brain: mostly functional.",
            "University, here I come."
    };

    //Variants pro Object
    public static final Map<String, List<String>> VARIANTS = Map.ofEntries(

            entry("bed", List.of(
                    "You consider lying down. Productive thoughts fade immediately.",
                    "This is not a bed. This is a trap.",
                    "Nice try. The bed will have to wait."
            )),
            entry("picture_frame", List.of(
                    "It’s straight. Suspiciously straight.",
                    "The picture refuses to explain itself.",
                    "You don’t remember putting this up."
            )),
            entry("window", List.of(
                    "You look outside. Freedom looks back.",
                    "A window. Very transparent. Very useless.",
                    "The window is doing an excellent job at being a window."
            )),
            entry("coffee_maker", List.of(
                    "Empty. Emotionally devastating.",
                    "Pressing buttons won’t help. This machine needs beans, not hope.",
                    "The coffee machine hums quietly. It knows it’s out of coffee."
            )),
            entry("chair", List.of(
                    "A chair. It’s doing its job admirably.",
                    "You give the chair a quick look. It appreciates the attention.",
                    "Sitting would be a commitment."
            )),
            entry("coffee", List.of(
                    "Cold coffee. The worst timeline.",
                    "Empty. A personal attack.",
                    "Cold. Empty. Judging you silently."
            )),
            entry("dog_food", List.of(
                    "A food bowl. Empty. Very on brand.",
                    "You check the bowl. Still empty. Shocking.",
                    "Nothing inside. This will definitely not solve itself."
            )),
            entry("tv", List.of(
                    "The TV is off. It appears to be in a reflective mood.",
                    "It’s turned off. And somehow it’s still distracting you.",
                    "You stare at it. It stares back. Nobody wins."
            )),
            entry("picture", List.of(
                    "At the university, a nerdy student spent most of his days surrounded by three towers of books, " +
                            "focused on one upcoming exam.\n" +
                            "In his backpack were two notebooks, five textbooks, and four folders filled with notes.\n",
                    "During late-night study sessions, four friends joined him with five cups of coffee.\n" +
                            "Together they reviewed three chapters, solved two practice tests, " +
                            "and finally understood one difficult subject.",
                    "Back at home, five books lay open beside two spare keys.\n" +
                            "Four alarms were set, one final revision was done, and three exams no longer felt impossible.."
//                    31254
//                    45321
//                    52413
            )),
            entry("lamp", List.of(
                    "Provides light. Not answers.",
                    "It’s on. Or off. Hard to tell.",
                    "You consider switching it. You don’t."
            )),
            entry("plant", List.of(
                    "It’s thriving purely out of spite.",
                    "This plant has more stability than your schedule.",
                    "Still a plant. It seems perfectly content with its life choices."
            )),
            entry("clock", List.of(
                    "It’s working. Unfortunately.",
                    "Yes, it’s a clock. It’s doing clock things.",
                    "A clock. It keeps time very professionally."
            )),
            entry("telephone", List.of(
                    "Nobody calls anymore.",
                    "You briefly consider calling for help.",
                    "It remains disappointingly silent."
            )),
            entry("shelves", List.of(
                    "Full of unread ambition.",
                    "So many books. So little follow-through.",
                    "You swear you’ll read them someday."
            )),
            entry("couch", List.of(
                    "This is where productivity goes to die.",
                    "You consider sitting down. The story urges you onward.",
                    "Yes, it’s a couch. No, it’s not part of the puzzle."
            )),
            entry("trash", List.of(
                    "Mostly empty. Like your motivation.",
                    "You immediately regret looking.",
                    "Nothing useful in there. You checked."
            )),
            entry("vase", List.of(
                    "Still alive. Barely.",
                    "A flower doing its best.",
                    "You admire its optimism."
            )),
            entry("exit", List.of(
                    "You can't leave yet. Your to-do list is still judging you.",
                    "The door is locked by… responsibility.",
                    "Leaving now would be… optimistic."
            )),
            entry("bathroom", List.of(
                    "You don’t need the bathroom. You need focus.",
                    "Bathroom’s closed. Emotionally, too.",
                    "Nope. Not a bathroom situation."
            )),
            entry("bookcase", List.of(
                    "A short storie can help you solve the puzzle!"
            ))
    );

    //Klicke 4 - 9
    public static final String GENERIC_AFTER_VARIANTS =
            "You gotta stop procrastinating …";

    //Klick ab 10
    public static final String GENERIC_AFTER_TEN =
            "Your patience is admirable.";
}
