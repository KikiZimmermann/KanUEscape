package at.ac.hcw.kanuescape.game;

//examines rules, triggers changes

public class KochManager {
    public enum TortillaState {
        START,
        INGREDIENTS_COLLECTED,
        WASHED,
        CUT,
        MEAT_FRIED,
        SEASONED,
        TORTILLAS_READY,
        FILLED,
        FINISHED
    }
    private TortillaState state = TortillaState.START;

    public String fridge() {
        if (state == TortillaState.START) {
            state = TortillaState.INGREDIENTS_COLLECTED;
            return "Fleisch und Tomaten aus dem Kuehlschrank geholt.";
        }
        return "Du hast schon alles was du aus dem Kuehlschrank brauchst.";
    }

    public String water() {
        if (state == TortillaState.INGREDIENTS_COLLECTED) {
            state = TortillaState.WASHED;
            return "Tomaten gewaschen.";
        } else if (state == TortillaState.START) {
            return "Erst Zutaten holen.";
        }
        return "Du hast schon alles abgewaschen.";
    }

    public String board() {
        return switch (state) {
            case WASHED -> {
                state = TortillaState.CUT;
                yield "Tomaten geschnitten.";
            }
            case FILLED -> {
                state = TortillaState.FINISHED;
                yield "Tortillas fertig.";
            }
            case CUT,MEAT_FRIED,SEASONED,TORTILLAS_READY -> {
                yield "Du musst gerade nichts mit den Tomaten hier machen.";
            }
            case FINISHED -> {
                yield "Schneid dich lieber nicht.";
            }
            default -> "Die Tomaten sind noch schmutzig.";
        };
    }

    public String stove() {
        return switch (state) {
            case CUT -> {
                state = TortillaState.MEAT_FRIED;
                yield "Fleisch angebraten.";
            }
            case SEASONED,MEAT_FRIED -> {
                yield "Du musst gerade nichts mit dem Fleisch hier machen.";
            }
            case TORTILLAS_READY -> {
                state = TortillaState.FILLED;
                yield "Tortillas gefuellt.";
            }
            default -> "Der Herd ist gerade nutzlos.";
        };
    }

    public String shelf() {
        if (state == TortillaState.MEAT_FRIED) {
            state = TortillaState.SEASONED;
            return "Gewuerze hinzugefuegt.";
        }
        return "Kein Grund zu wuerzen.";
    }

    public String cabinet() {
        if (state == TortillaState.SEASONED) {
            state = TortillaState.TORTILLAS_READY;
            return "Tortillas aus dem Schrank genommen.";
        } else if (state == TortillaState.TORTILLAS_READY||state == TortillaState.FILLED||state == TortillaState.FINISHED) {
            return "Du hast dir schon eine Tortilla genommen.";
        }
        return "Du brauchst zuerst eine Füllung.";
    }

    public TortillaState getState() {
        return state;
    }
    public TortillaState getFINISHED() {
        return state.FINISHED;
    }
}
