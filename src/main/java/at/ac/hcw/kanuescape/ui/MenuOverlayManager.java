package at.ac.hcw.kanuescape.ui;

import at.ac.hcw.kanuescape.controller.ui.MenuScreenController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class MenuOverlayManager {

    private final StackPane overlayLayer;

    private Node menuNode;
    private MenuScreenController controller;

    private boolean open = false;

    // states (später an AudioManager weitergeben)
    private boolean musicEnabled = true;
    private boolean sfxEnabled = true;

    // callbacks nach außen (GameController / App)
    private Runnable onNewGame = () -> {};
    private Runnable onExit = () -> {};
    private Runnable onShowEndScreen = () -> {};



    public MenuOverlayManager(StackPane overlayLayer) {
        this.overlayLayer = overlayLayer;
    }

    public void load() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ui/MenuScreen.fxml"));
            menuNode = loader.load();
            controller = loader.getController();

            controller.setCallbacks(
                    this::close,           // Continue
                    () -> onNewGame.run(),  // New Game
                    () -> onExit.run()      // Exit
            );



            controller.setInitial(musicEnabled, sfxEnabled);

            // start hidden
            menuNode.setVisible(false);
            menuNode.setManaged(false);

            overlayLayer.getChildren().add(menuNode);

            // overlayLayer soll immer die ganze Fläche nehmen
            overlayLayer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

// menuNode (menuRoot) soll auch die ganze Fläche nehmen (für dim + Zentrierung)
            if (menuNode instanceof Region r) {
                r.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            }


        } catch (Exception e) {
            throw new RuntimeException("Failed to load menu overlay FXML", e);
        }
    }

    public void setOnNewGame(Runnable r) { this.onNewGame = (r != null) ? r : () -> {}; }
    public void setOnExit(Runnable r)    { this.onExit = (r != null) ? r : () -> {}; }
    public void setOnShowEndScreen(Runnable r) {
        this.onShowEndScreen = (r != null) ? r : () -> {};
    }

    public boolean isOpen() { return open; }
    public boolean isPaused() { return open; } // Menu pausiert das Spiel

    public boolean isMusicEnabled() { return musicEnabled; }
    public boolean isSfxEnabled() { return sfxEnabled; }

    public void toggle() {
        if (open) close();
        else open();
    }

    public void open() {
        if (menuNode == null) return;

        open = true;

        overlayLayer.setVisible(true);
        overlayLayer.setManaged(true);

        menuNode.setVisible(true);
        menuNode.setManaged(true);

        menuNode.requestFocus();
    }

    public void close() {
        if (menuNode == null) return;

        // Werte vom Menü holen
        if (controller != null) {
            musicEnabled = controller.isMusicOn();
            sfxEnabled = controller.isSfxOn();
        }

        open = false;

        menuNode.setVisible(false);
        menuNode.setManaged(false);

        overlayLayer.setVisible(false);
        overlayLayer.setManaged(false);
    }


}
