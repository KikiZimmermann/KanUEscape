package at.ac.hcw.kanuescape.controller.ui;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class StartScreenController {

    @FXML
    private StackPane startRoot;

    private Runnable onStart;

    public void setOnStart(Runnable onStart) {
        this.onStart = onStart;
    }

    @FXML
    private void onStartClicked() {
        if (onStart != null) {
            onStart.run();
        }
    }

    public void hide() {
        startRoot.setVisible(false);
        startRoot.setManaged(false);
    }
}
