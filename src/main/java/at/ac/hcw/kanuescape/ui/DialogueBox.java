package at.ac.hcw.kanuescape.ui;

import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class DialogueBox extends AnchorPane {

    private final StackPane dialogueContainer = new StackPane();

    private final ImageView dialogueBoxImage = new ImageView();
    private final Label dialogueText = new Label();

    private final StackPane dialogueArrowWrapper = new StackPane();
    private final ImageView dialogueArrow = new ImageView();

    private TranslateTransition arrowBounce;

    public DialogueBox() {
        // overlay styling / behaviour
        setVisible(false);
        setManaged(false);
        setPickOnBounds(true);
        setStyle("-fx-background-color: rgba(0,0,0,0.15);");

        // click closes
        setOnMouseClicked(e -> hide());

        // container anchoring
        AnchorPane.setLeftAnchor(dialogueContainer, 0.0);
        AnchorPane.setRightAnchor(dialogueContainer, 0.0);
        AnchorPane.setBottomAnchor(dialogueContainer, -5.0);
        dialogueContainer.setAlignment(javafx.geometry.Pos.BOTTOM_CENTER);

        // textbox image
        dialogueBoxImage.setPreserveRatio(true);
        dialogueBoxImage.setSmooth(false);

        // text label
        dialogueText.setWrapText(true);
        dialogueText.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        StackPane.setAlignment(dialogueText, javafx.geometry.Pos.BOTTOM_LEFT);

        // arrow
        dialogueArrow.setPreserveRatio(true);
        dialogueArrow.setSmooth(false);

        StackPane.setAlignment(dialogueArrowWrapper, javafx.geometry.Pos.BOTTOM_RIGHT);
        dialogueArrowWrapper.getChildren().add(dialogueArrow);

        // build stack
        dialogueContainer.getChildren().addAll(dialogueBoxImage, dialogueText, dialogueArrowWrapper);
        getChildren().add(dialogueContainer);

        // Images laden
        dialogueBoxImage.setImage(new Image(getClass().getResourceAsStream("/assets/images/ui/textfield.png")));
        dialogueArrow.setImage(new Image(getClass().getResourceAsStream("/assets/images/ui/text_arrow.png")));

        // Bounce
        startArrowBounce();
    }

    /** Bindings, damit Box + Text + Arrow beim Resize “an der Box kleben” */
    public void bindToRoot(Region root) {
        // Boxbreite = 85% Fensterbreite
        dialogueBoxImage.fitWidthProperty().bind(root.widthProperty().multiply(0.85));

        //designbreite der box als refernezwert
        final double BASE_BOX_W = 800;

        // deine “guten” Pixelwerte (wo was ist) wenn die box 800 groß ist
        final double TEXT_X_PX = 105;
        final double TEXT_Y_PX = -140;

        final double ARROW_X_PX = 260;
        final double ARROW_Y_PX = 50;

        final double TEXT_MAXW_PX = 650;        // zeilenumbruch
        final double ARROW_H_PX = 22;           // arrow höhe

        // ratios fürs mitskalieren
        final double textXRatio = TEXT_X_PX / BASE_BOX_W;
        final double textYRatio = TEXT_Y_PX / BASE_BOX_W;

        final double arrowXRatio = ARROW_X_PX / BASE_BOX_W;
        final double arrowYRatio = ARROW_Y_PX / BASE_BOX_W;

        final double textMaxWRatio = TEXT_MAXW_PX / BASE_BOX_W;
        final double arrowHRatio   = ARROW_H_PX / BASE_BOX_W;

        // Text position in box bleibt wie gehabt (gebunden)
        dialogueText.translateXProperty().bind(dialogueBoxImage.fitWidthProperty().multiply(textXRatio));
        dialogueText.translateYProperty().bind(dialogueBoxImage.fitWidthProperty().multiply(textYRatio));
        dialogueText.maxWidthProperty().bind(dialogueBoxImage.fitWidthProperty().multiply(textMaxWRatio));

        // Arrow size gebunden
        dialogueArrow.fitHeightProperty().bind(dialogueBoxImage.fitWidthProperty().multiply(arrowHRatio));

        // Arrow position gebunden
        dialogueArrowWrapper.translateXProperty().bind(dialogueBoxImage.fitWidthProperty().multiply(arrowXRatio));
        dialogueArrowWrapper.translateYProperty().bind(dialogueBoxImage.fitWidthProperty().multiply(arrowYRatio));

        // Font scaling
        dialogueText.styleProperty().bind(Bindings.createStringBinding(() -> {
            double w = dialogueBoxImage.getFitWidth();
            double size = w * (16.0 / 800.0);
            return "-fx-text-fill: white; -fx-font-size: " + size + "px;";
        }, dialogueBoxImage.fitWidthProperty()));
    }

    public void show(String text) {
        dialogueText.setText(text);
        setManaged(true);
        setVisible(true);
        if (arrowBounce != null) arrowBounce.play();
    }

    public void hide() {
        setVisible(false);
        setManaged(false);
        if (arrowBounce != null) arrowBounce.stop();
    }

    public boolean isVisibleBox() {
        return isVisible();
    }

    private void startArrowBounce() {
        double baseY = dialogueArrow.getTranslateY();
        double jumpY = baseY - 6;

        arrowBounce = new TranslateTransition(Duration.millis(400), dialogueArrow);
        arrowBounce.setFromY(baseY);
        arrowBounce.setToY(jumpY);
        arrowBounce.setAutoReverse(true);
        arrowBounce.setCycleCount(TranslateTransition.INDEFINITE);
    }
}
