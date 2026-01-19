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
            return "Meat and tomatoes taken from the fridge.";
        }
        return "You already took everything you need from the fridge.";
    }

    public String water() {
        if (state == TortillaState.INGREDIENTS_COLLECTED) {
            state = TortillaState.WASHED;
            return "Tomatoes washed.";
        } else if (state == TortillaState.START) {
            return "Get the ingredients first.";
        }
        return "Everything is already washed.";
    }

    public String board() {
        return switch (state) {
            case WASHED -> {
                state = TortillaState.CUT;
                yield "Tomatoes cut.";
            }
            case FILLED -> {
                state = TortillaState.FINISHED;
                yield "Tortillas are finished.";
            }
            case CUT,MEAT_FRIED,SEASONED,TORTILLAS_READY -> {
                yield "There is nothing to do with the tomatoes right now.";
            }
            case FINISHED -> {
                yield "Better not cut yourself.";
            }
            default -> "The tomatoes are still dirty.";
        };
    }

    public String stove() {
        return switch (state) {
            case CUT -> {
                state = TortillaState.MEAT_FRIED;
                yield "Meat fried.";
            }
            case SEASONED,MEAT_FRIED -> {
                yield "There is nothing to do with the meat right now.";
            }
            case TORTILLAS_READY -> {
                state = TortillaState.FILLED;
                yield "Tortillas filled.";
            }
            default -> "The stove is useless right now.";
        };
    }

    public String shelf() {
        if (state == TortillaState.MEAT_FRIED) {
            state = TortillaState.SEASONED;
            return "Spices added.";
        }
        return "No reason to season anything.";
    }

    public String cabinet() {
        if (state == TortillaState.SEASONED) {
            state = TortillaState.TORTILLAS_READY;
            return "Tortillas taken from the cabinet.";
        } else if (state == TortillaState.TORTILLAS_READY||state == TortillaState.FILLED||state == TortillaState.FINISHED) {
            return "You already took a tortilla.";
        }
        return "You need a filling first.";
    }

    public TortillaState getState() {
        return state;
    }
    public TortillaState getFINISHED() {
        return state.FINISHED;
    }
}
