package at.ac.hcw.kanuescape;

import at.ac.hcw.kanuescape.controller.GameController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

/**
 * GameApp
 *
 * Einstiegspunkt der JavaFX-Anwendung.
 *
 * Verantwortlich für:
 * - Starten von JavaFX
 * - Laden der Haupt-FXML
 * - Erzeugen der Scene
 * - Anzeigen des Hauptfensters (Stage)
 *
 * Keine Game-Logik, kein Rendering, kein Controller-Code.
 */

public class GameApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // FXML laden (UI + Controller werden hier initialisiert)
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/Game.fxml")
        );

        Scene scene = new Scene(loader.load());

        // Get and initialize controller (input + game loop); Mvm
        GameController controller = loader.getController();
        controller.init(scene);

        // Fenster konfigurieren
        stage.setTitle("KanUEscape");
        stage.setScene(scene);

        // Fenster- & Taskleisten-Icon setzen
        stage.getIcons().add(
                new Image(getClass().getResourceAsStream("/assets/images/icon/icon.png"))
        );

        stage.show();
    }

    /**
     * Klassischer Java-Einstiegspunkt.
     * Ruft intern Application.start(...) auf.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
