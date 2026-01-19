package at.ac.hcw.kanuescape.controller.ui;

import javafx.fxml.FXML;

public class EndScreenController {

    private Runnable onNewGame = () -> {};
    private Runnable onExit = () -> {};

    public void setCallbacks(Runnable onNewGame, Runnable onExit) {
        this.onNewGame = (onNewGame != null) ? onNewGame : () -> {};
        this.onExit = (onExit != null) ? onExit : () -> {};
    }

}
