package at.ac.hcw.kanuescape.controller;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class LaptopController {
    @FXML
    private ImageView exitImage;
    @FXML
    protected void Exit() {
        // Holt das Stage vom Image
        Stage stage = (Stage) exitImage.getScene().getWindow();
        stage.close();
    }
}
