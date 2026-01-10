package at.ac.hcw.kanuescape.controller;

import at.ac.hcw.kanuescape.game.Player; // Mvm
import at.ac.hcw.kanuescape.tiled.MapLoader;
import at.ac.hcw.kanuescape.tiled.MapRenderer;
import at.ac.hcw.kanuescape.tiled.TiledModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.animation.AnimationTimer; // Mvm
import at.ac.hcw.kanuescape.tiled.RenderContext;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.input.KeyCode; // Mvm
import java.util.EnumMap; // Mvm
import java.util.Map; // Mvm

/**
 * GameController
 *
 * - Lädt Map + Tileset + Player-Sprite aus /resources
 * - Bindet Canvas an das Fenster (resizable) mit Rahmen (Padding)
 * - Rendert Map-Layer + Player (noch ohne Bewegung)
 *
 * Hinweis: Parsing & Tileset-Logik steckt in MapLoader/TiledModel.
 * Rendering der TileLayer steckt in MapRenderer.
 */

public class GameController {

    // Ressourcenpfade
    private static final String MAP_PATH = "/assets/maps/game_screen.json";
    private static final String TSX_PATH = "/assets/tilesets/game_screen.tsx";
    private static final String TILESET_IMAGE_PATH = "/assets/images/tileset/tileset.png";
    private static final String PLAYER_SPRITE_PATH = "/assets/images/sprite/nerdyguy_sprite.png";

    // Look & Layout
    private static final Color BACKGROUND = Color.web("#4e4e4e");
    private static final double FRAME_PADDING = 25; // muss zum FXML -fx-padding passen

    // Sprite Sheet Setup (3x4 = 12 Frames)
//    private static final int SPRITE_COLS = 3;
//    private static final int SPRITE_ROWS = 4;

    private Player player;

    // "Optischer" Offset: Sprites sonst nicht "zentriert" im Tile
    private static final double PLAYER_Y_ANCHOR = 0.10;

    // Engine-Teile
    private final MapRenderer renderer = new MapRenderer();

    // Map als Feld speichern, damit render() später damit arbeiten kann
    private TiledModel.TiledMap map;
    private TiledModel.TsxTileset tsx;
    private Image tilesetImage;

    // --- Player-State (aktuell fester Frame) ---
//    private int playerTileX = 5;
//    private int playerTileY = 4;
//    private int playerFrameCol = 1;
//    private int playerFrameRow = 2;

    // Player sprite
    private Image playerSprite;

    //to DoListe
    private Stage todoStage;
    private ToDoListeController todoController;

    //render Context für größen
    private RenderContext renderContext;
    //Die Objekt Layer wird hier gespeichert
    private TiledModel.TiledLayer interactionLayer;

    @FXML private StackPane root;
    @FXML private Canvas gameCanvas;

    @FXML
    private void initialize() throws Exception{

        // Canvas folgt der Größe des Containers, bleibt aber innen "kleiner" (Rahmen bleibt sichtbar)
        gameCanvas.widthProperty().bind(root.widthProperty().subtract(FRAME_PADDING * 2));
        gameCanvas.heightProperty().bind(root.heightProperty().subtract(FRAME_PADDING * 2));

        // Ressourcen laden (ohne UI-Logik)
        map = MapLoader.loadMap(MAP_PATH);
        tsx = MapLoader.loadTsxTileset(TSX_PATH);
        tilesetImage = MapLoader.loadImage(TILESET_IMAGE_PATH);
        playerSprite = new Image(getClass().getResourceAsStream(PLAYER_SPRITE_PATH));

        // Erst rendern, wenn Layout fertig ist (Canvas ist sonst oft 0x0)
        Platform.runLater(this::render);

        // Bei Resize neu rendern (wir machen kein Game-Loop, sondern "on demand")
        gameCanvas.widthProperty().addListener((obs, oldV, newV) -> render());
        gameCanvas.heightProperty().addListener((obs, oldV, newV) -> render());

 // Fehler bei Einfügen
        // Mvm; initialize player; start position for now (5,4)
        player = new Player(5,4);
        player.setSpeedTilesPerSecond(4.0);
        player.setFrameDurationMs(120); // ms; per animation step, might have to change after test

        // ToDoListe Laden
        FXMLLoader fxmlLoader = new FXMLLoader(GameController.class.getResource("/fxml/toDoListe.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 339, 511);
        todoController = fxmlLoader.getController();
        scene.setFill(Color.TRANSPARENT);
        todoStage = new Stage();
        todoStage.setAlwaysOnTop(true);
        todoStage.setResizable(false);
        todoStage.initStyle(StageStyle.TRANSPARENT);
        todoStage.setX(1000);
        todoStage.setY(100);
        todoStage.setTitle("ToDoListe!");
        todoStage.setScene(scene);
    }

    /*
     * Rendert ein komplettes Frame: Background -> Map-Layer -> Player.
     * (Kein Game-Loop, nur beim Start und beim Resize)
     */
    private void render() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.setImageSmoothing(false); // wichtig gegen "Seams" beim Skalieren

        double w = gameCanvas.getWidth();
        double h = gameCanvas.getHeight();

        // Background füllen (Rahmenfarbe innerhalb der Canvas)
        gc.setFill(BACKGROUND);
        gc.fillRect(0, 0, w, h);

        if (map == null || tsx == null || tilesetImage == null) {
            return; // Assets noch nicht geladen
        }

        int firstGid = map.tilesets().get(0).firstgid();
        int columns = tsx.columns();

        // Sichtbare Tile-Layer in richtiger Reihenfolge
        renderLayerByName(gc, "floor", firstGid, columns);
        renderLayerByName(gc, "floor_help", firstGid, columns);
        renderLayerByName(gc, "objects_back", firstGid, columns);
        renderLayerByName(gc, "objects", firstGid, columns);
        renderLayerByName(gc, "objects_front", firstGid, columns);

        // Player darüber zeichnen
        renderPlayer(gc);
    }


    /*
     Findet einen Layer anhand des Namens und rendert ihn, wenn es ein TileLayer ist.
     */
    private void renderLayerByName(GraphicsContext gc, String layerName, int firstGid, int columns) {
        for (var layer : map.layers()) {
            if (layer.isTileLayer() && layerName.equals(layer.name())) {
                RenderContext rc = renderer.renderTileLayer(gc, map, layer, tilesetImage, firstGid, columns);

                // Wir merken uns EINEN Layer für Interaktionen
                if ("objects".equals(layerName)) {
                    renderContext = rc;
                    interactionLayer = layer;
                }
                return;
            }
        }
    }

    /**
     * Rendert den Player als EIN Frame aus dem Sprite Sheet.
     * Position ist Tile-basiert und wird auf das gleiche Integer-Grid gelegt wie die Map,
     * damit nichts "driftet".
     */
    private void renderPlayer(GraphicsContext gc) {
        if (playerSprite == null || map == null || player == null) return;

        int tileW = map.tilewidth();
        int tileH = map.tileheight();

        double canvasW = gameCanvas.getWidth();
        double canvasH = gameCanvas.getHeight();

        double mapW = map.width() * tileW;
        double mapH = map.height() * tileH;

        // Fit-to-window scale (Map bleibt komplett sichtbar)
        double scale = Math.min(canvasW / mapW, canvasH / mapH);

        double renderW = mapW * scale;
        double renderH = mapH * scale;

        double offsetX = (canvasW - renderW) / 2.0;
        double offsetY = (canvasH - renderH) / 2.0;

        // Integer-Grid (wie MapRenderer)
        int baseX = (int) Math.round(offsetX);
        int baseY = (int) Math.round(offsetY);
        int scaledTileW = (int) Math.round(tileW * scale);
        int scaledTileH = (int) Math.round(tileH * scale);

        // Sprite-Frame (Quelle)
        double frameW = playerSprite.getWidth() / Player.SPRITE_COLS;
        double frameH = playerSprite.getHeight() / Player.SPRITE_ROWS;
        double sx = player.getFrameCol() * frameW;
        double sy = player.getFrameRow() * frameH;

        // Zielgröße (1.2 Tiles hoch, proportional)
        double aspect = frameW / frameH;
        int targetH = (int) Math.round(scaledTileH * 1.2);
        int targetW = (int) Math.round(targetH * aspect);

        // Player position tiles -> px
        double tileX = player.getTileX();
        double tileY = player.getTileY();
        int tilePxX = baseX + (int) Math.round(tileX * scaledTileW);
        int tilePyY = baseY + (int) Math.round(tileY * scaledTileH);

        // Centered in tile
        int dx = tilePxX + (scaledTileW - targetW) / 2;
        int dy = tilePyY + (scaledTileH - targetH) / 2;

        // Optical anchor towards top (against "bottom-heavy" impression - "centered")
        dy -= (int) Math.round(scaledTileH * PLAYER_Y_ANCHOR);
        // Drawing (source: sx, sy, frameW, frameH -> destination: dx, dy, targetW, targetH)
        gc.drawImage(playerSprite, sx, sy, frameW, frameH, dx, dy, targetW, targetH);

        gameCanvas.setOnMouseClicked(e -> {
            handleMapClick(e.getX(), e.getY());
        });
    }

    // Mvm
    private final Map<KeyCode, Boolean> keys = new EnumMap<>(KeyCode.class);
    private AnimationTimer loop;

    // Mvm
    public void init(Scene scene) {
        // Key states (WASD)
        scene.setOnKeyPressed(e -> keys.put(e.getCode(), true));
        scene.setOnKeyReleased(e -> keys.put(e.getCode(), false));

        // Focus: ensure that key events arrive
        root.setOnMouseClicked( e -> root.requestFocus());
        root.requestFocus();

        // Game-loop: requests render() on every frame
        loop = new AnimationTimer() {
            long last = 0;
            @Override
            public void handle(long now) {
                if (last == 0) {last  = now; return;}
                double dt = (now - last) / 1_000_000_000.0; // time delta in seconds
                last = now;

                boolean up = isDown(KeyCode.W);
                boolean down = isDown(KeyCode.S);
                boolean left = isDown(KeyCode.A);
                boolean right = isDown(KeyCode.D);
                // Player update with time delta and input
                player.update(dt, up, down, left, right);
                // Animation (idle vs moving)
                player.animate(now, up || down || left || right);
                render();
            }
        };
        loop.start();
    }

    private boolean isDown(KeyCode code) {
        return keys.getOrDefault(code, false);
    }

    private void handleMapClick(double mouseX, double mouseY) {

        if (renderContext == null || interactionLayer == null) return;

        // Klick relativ zur Map
        double localX = mouseX - renderContext.baseX();
        double localY = mouseY - renderContext.baseY();

        if (localX < 0 || localY < 0) return;

        int tileX = (int) (localX / renderContext.tileW());
        int tileY = (int) (localY / renderContext.tileH());

        if (tileX < 0 || tileY < 0 ||
                tileX >= interactionLayer.width() ||
                tileY >= interactionLayer.height()) {
            return;
        }

        int index = tileY * interactionLayer.width() + tileX;
        int gid = interactionLayer.data()[index];

        onTileClicked(tileX, tileY, gid);
    }

    private void onTileClicked(int x, int y, int gid) {
        if (gid == 0) return;

        System.out.println("Tile geklickt: (" + x + "," + y + ") GID=" + gid);
        if(gid == 60 || gid == 72){
            if (todoStage != null) {
                todoStage.show();
            }
        }
    }

    public void CheckBuecher() {
        if (todoController != null) {
            todoController.CheckBuecher(true);
        }
    }
    @FXML
    public void CheckKochen() {
        if (todoController != null) {
            todoController.CheckKochen(true);
        }
    }
    @FXML
    public void CheckMathe() {
        if (todoController != null) {
            todoController.CheckMathe(true);
        }
    }
    @FXML
    public void CheckProg() {
        if (todoController != null) {
            todoController.CheckProg(true);
        }
    }

}
