package at.ac.hcw.kanuescape.controller.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class MenuScreenController {

    @FXML private StackPane menuRoot;
    @FXML private StackPane menuBox;

    @FXML private ImageView musicCheckmark;
    @FXML private ImageView sfxCheckmark;

    private boolean musicOn = true;
    private boolean sfxOn = true;

    private Runnable onContinue;
    private Runnable onNewGame;
    private Runnable onExit;

    @FXML
    private void initialize() {
        menuRoot.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        menuRoot.layoutBoundsProperty().addListener((obs, o, n) -> applyScale());
        Platform.runLater(this::applyScale);
    }
    private void applyScale() {
        if (menuRoot == null || menuBox == null) return;

        double scale = 1.15; // <- größer / kleiner nach Geschmack

        // damit es nie aus dem Fenster rausläuft:
        double maxScaleX = menuRoot.getWidth()  / menuBox.getPrefWidth();
        double maxScaleY = menuRoot.getHeight() / menuBox.getPrefHeight();
        scale = Math.min(scale, Math.min(maxScaleX, maxScaleY));

        menuBox.setScaleX(scale);
        menuBox.setScaleY(scale);

        // WICHTIG: keine "Zentrier-Translate" mehr!
        menuBox.setTranslateX(0);
        menuBox.setTranslateY(0);
    }

    public void setCallbacks(Runnable onContinue, Runnable onNewGame, Runnable onExit) {
        this.onContinue = onContinue;
        this.onNewGame = onNewGame;
        this.onExit = onExit;
    }

    public void setInitial(boolean musicOn, boolean sfxOn) {
        this.musicOn = musicOn;
        this.sfxOn = sfxOn;
        refresh();
    }

    public boolean isMusicOn() { return musicOn; }
    public boolean isSfxOn() { return sfxOn; }

    private void refresh() {
        if (musicCheckmark != null) musicCheckmark.setVisible(musicOn);
        if (sfxCheckmark != null) sfxCheckmark.setVisible(sfxOn);
    }

    @FXML private void toggleMusic() {
        musicOn = !musicOn;
        refresh();
    }

    @FXML private void toggleSfx() {
        sfxOn = !sfxOn;
        refresh();
    }

    @FXML private void onContinue() {
        if (onContinue != null) onContinue.run();
    }

    @FXML private void onNewGame() {
        if (onNewGame != null) onNewGame.run();
    }

    @FXML private void onExit() {
        if (onExit != null) onExit.run();
    }
}
