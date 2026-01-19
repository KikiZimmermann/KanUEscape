package at.ac.hcw.kanuescape.controller.ui;

import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class DialogueBoxController {

    // root of the overlay (the fxml StackPane)
    @FXML
    private StackPane overlayRoot;

    // the big textbox image
    @FXML
    private ImageView boxImg;

    // the text inside the box
    @FXML
    private Label dialogueText;

    // the small arrow
    @FXML
    private ImageView arrowImg;
    @FXML private StackPane arrowWrap;

    private TranslateTransition hop;


    @FXML
    private void initialize() {
            // --- Text ---
            dialogueText.setWrapText(true);
            dialogueText.setMaxWidth(520);          // Textbreite IN der Box
            dialogueText.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

            // leichte optische Verschiebung
            dialogueText.setTranslateX(-100);
            dialogueText.setTranslateY(-150);

            // --- Arrow ---
            arrowImg.setFitHeight(30);
            arrowImg.setPreserveRatio(true);

            arrowWrap.setTranslateX(-270);
            arrowWrap.setTranslateY(-50);

            // Hop Animation
            hop = new TranslateTransition(Duration.millis(450), arrowImg);
            hop.setAutoReverse(true);
            hop.setCycleCount(TranslateTransition.INDEFINITE);
            hop.setByY(-6);
            hop.play();
        }



    public void setText(String text) {
        dialogueText.setText(text);
    }

}
