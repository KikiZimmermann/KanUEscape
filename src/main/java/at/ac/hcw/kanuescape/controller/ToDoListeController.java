package at.ac.hcw.kanuescape.controller;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class ToDoListeController {
    @FXML
    private ImageView exitImage;
    @FXML
    private ImageView checkKochen;
    @FXML
    private ImageView checkMathe;
    @FXML
    private ImageView checkBuecher;
    @FXML
    private ImageView checkProg;
    @FXML
    protected void Exit() {
        // Holt das Stage vom Image
        Stage stage = (Stage) exitImage.getScene().getWindow();
        stage.close();
    }

    public void CheckBuecher(boolean t) {
        if (checkBuecher != null) {
            checkBuecher.setVisible(t);
            System.out.println("geht");
        }
    }
    public void CheckKochen(boolean t) {
        if (checkKochen != null) {
            checkKochen.setVisible(t);
        }
    }
    public void CheckMathe(boolean t) {
        if (checkMathe != null) {
            checkMathe.setVisible(t);
        }
    }
    public void CheckProg(boolean t) {
        if (checkProg != null) {
            checkProg.setVisible(t);
        }
    }

    // reset methode für New Game
    public void resetChecks() {
        CheckKochen(false);
        CheckMathe(false);
        CheckBuecher(false);
        CheckProg(false);
    }
}
