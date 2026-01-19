package at.ac.hcw.kanuescape.ui;

import at.ac.hcw.kanuescape.controller.ui.EndScreenController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class EndScreenOverlayManager {

    private final StackPane overlayLayer;

    private Node node;
    private EndScreenController controller;
    private boolean open = false;

    private Runnable onNewGame = () -> {};
    private Runnable onExit = () -> {};

    public EndScreenOverlayManager(StackPane overlayLayer) {
        this.overlayLayer = overlayLayer;
    }

    public void load() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ui/EndScreen.fxml"));
            node = loader.load();
            controller = loader.getController();

            controller.setCallbacks(
                    () -> onNewGame.run(),
                    () -> onExit.run()
            );

            node.setVisible(false);
            node.setManaged(false);

            overlayLayer.getChildren().add(node);

            overlayLayer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            if (node instanceof Region r) r.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load end screen FXML", e);
        }
    }

    public void setOnNewGame(Runnable r) { this.onNewGame = (r != null) ? r : () -> {}; }
    public void setOnExit(Runnable r) { this.onExit = (r != null) ? r : () -> {}; }

    public boolean isOpen() { return open; }

    public void open() {
        if (node == null) return;
        open = true;

        overlayLayer.setVisible(true);
        overlayLayer.setManaged(true);

        node.setVisible(true);
        node.setManaged(true);

        node.requestFocus();
    }

    public void close() {
        if (node == null) return;
        open = false;

        node.setVisible(false);
        node.setManaged(false);

        overlayLayer.setVisible(false);
        overlayLayer.setManaged(false);
    }
}
