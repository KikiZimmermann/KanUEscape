package at.ac.hcw.kanuescape.controller;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class SchrankController {
    @FXML
    private ImageView exitImage;
    @FXML
    protected void Exit() {
        Stage stage = (Stage) exitImage.getScene().getWindow();
        stage.close();
    }
}