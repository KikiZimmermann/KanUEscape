package at.ac.hcw.kanuescape.controller.ui;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class EndScreenController {

    @FXML private StackPane endRoot;
    @FXML private StackPane endBox;

    private Runnable onNewGame = () -> {};
    private Runnable onExit = () -> {};


    public void setCallbacks(Runnable onNewGame, Runnable onExit) {
        this.onNewGame = (onNewGame != null) ? onNewGame : () -> {};
        this.onExit = (onExit != null) ? onExit : () -> {};
    }

}
