package at.ac.hcw.kanuescape.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class KuehlschrankController {
    @FXML
    private ImageView exitImage;
    @FXML
    protected void Exit() {
        Stage stage = (Stage) exitImage.getScene().getWindow();
        stage.close();
    }
}