package at.ac.hcw.kanuescape.ui;

import at.ac.hcw.kanuescape.controller.ui.StartScreenController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class StartScreenOverlayManager {

    private final StackPane layer;
    private Parent node;
    private StartScreenController controller;

    private Runnable onStart;

    public StartScreenOverlayManager(StackPane layer) {
        this.layer = layer;
    }

    public void load() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/ui/StartScreen.fxml")
            );
            node = loader.load();
            controller = loader.getController();

            // in Layer einhängen
            layer.getChildren().setAll(node);

            // initial sichtbar
            open();

            // Button -> Callback
            controller.setOnStart(() -> {
                if (onStart != null) onStart.run();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOnStart(Runnable onStart) {
        this.onStart = onStart;
    }

    public void open() {
        layer.setVisible(true);
        layer.setManaged(true);
        layer.setPickOnBounds(true);
    }

    public void close() {
        layer.setVisible(false);
        layer.setManaged(false);
    }

    public boolean isOpen() {
        return layer.isVisible();
    }
}
