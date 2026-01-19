package at.ac.hcw.kanuescape.controller.ui;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
    @FXML
    private StackPane arrowWrap;

    private TranslateTransition hop;
    private Timeline typing;
    private String fullText = "";
    private int charIndex = 0;
    private boolean typingInProgress = false;
    private boolean textFullyShown = true; // wenn true: darf "weiter/close"


    @FXML
    private void initialize() {
        // Text
        dialogueText.setWrapText(true);
        dialogueText.setMaxWidth(520);          // Textbreite IN der Box
        dialogueText.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

        // ✅ WICHTIG: nicht zentrieren, sondern oben-links fixieren
        StackPane.setAlignment(dialogueText, Pos.TOP_LEFT);
        // Statt translate (geht auch), sauberer ist Margin:
        StackPane.setMargin(dialogueText, new Insets(0, 0, 0, 0));

        // leichte optische Verschiebung
        dialogueText.setTranslateX(220);
        dialogueText.setTranslateY(590);

        // Arrow
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
        startTypewriter(text);
    }


    private void startTypewriter(String text) {
        // stoppe alte Animation
        if (typing != null) typing.stop();

        fullText = (text == null) ? "" : text;
        charIndex = 0;
        typingInProgress = true;
        textFullyShown = false;

        dialogueText.setText("");

        typing = new Timeline(new KeyFrame(Duration.millis(18), e -> { // speed
            charIndex++;
            if (charIndex >= fullText.length()) {
                dialogueText.setText(fullText);
                typingInProgress = false;
                textFullyShown = true;
                typing.stop();
            } else {
                dialogueText.setText(fullText.substring(0, charIndex));
            }
        }));
        typing.setCycleCount(Timeline.INDEFINITE);
        typing.playFromStart();
    }

    // returns true = text is fully shown AFTER this click
    public boolean onUserClick() {
        // 1) wenn gerade tippt -> sofort fertig anzeigen, NICHT schließen
        if (typingInProgress) {
            if (typing != null) typing.stop();
            dialogueText.setText(fullText);
            typingInProgress = false;
            textFullyShown = true;
            return true; // <-- skip happened
        }

        // 2) wenn schon voll -> "ready for next/close"
        return false; // <-- no skip, so caller may close/advance
    }

    public void stopTyping() {
        if (typing != null) typing.stop();
        typingInProgress = false;
        textFullyShown = true;
    }


}
