package at.ac.hcw.kanuescape.controller;

import at.ac.hcw.kanuescape.tiled.MapLoader;
import at.ac.hcw.kanuescape.tiled.MapRenderer;
import at.ac.hcw.kanuescape.tiled.TiledModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import java.util.EnumMap;
import java.util.Map;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import at.ac.hcw.kanuescape.game.Player; // Mvm
import javafx.animation.AnimationTimer; // Mvm
import javafx.scene.input.KeyCode; // Mvm
import javafx.scene.Scene; // Mvm

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
    private static final String TSX_PATH = "/assets/tiles/game_screen.tsx";
    private static final String TILESET_IMAGE_PATH = "/assets/tiles/tileset.png";
    private static final String PLAYER_SPRITE_PATH = "/assets/sprite/nerdyguy_sprite.png";

    // Look & Layout
    private static final Color BACKGROUND = Color.web("#4e4e4e");
    private static final double FRAME_PADDING = 25; // muss zum FXML -fx-padding passen

    // Player
    private Player player;
    private Image playerSprite;
    private static final double PLAYER_Y_ANCHOR = 0.10; // "Optical" Offset/Anchor: centers sprite in tile

    // Engine
    private final MapRenderer renderer = new MapRenderer();
    // Map als Feld speichern, damit render() später damit arbeiten kann
    private TiledModel.TiledMap map;
    private TiledModel.TsxTileset tsx;
    private Image tilesetImage;

    @FXML private StackPane root;
    @FXML private Canvas gameCanvas;

    // Input/loop
    private final Map<KeyCode, Boolean> keys = new EnumMap(KeyCode.class); // gotta understand this still
    private AnimationTimer loop;

    @FXML
    public void initialize() {
        // Canvas folgt der Größe des Containers, bleibt aber innen "kleiner" (Rahmen bleibt sichtbar)
        gameCanvas.widthProperty().bind(root.widthProperty().subtract(FRAME_PADDING * 2));
        gameCanvas.heightProperty().bind(root.heightProperty().subtract(FRAME_PADDING * 2));

        // Ressourcen laden (ohne UI-Logik)
        map = MapLoader.loadMap(MAP_PATH);
        tsx = MapLoader.loadTsxTileset(TSX_PATH);
        tilesetImage = MapLoader.loadImage(TILESET_IMAGE_PATH);
        playerSprite = new Image(getClass().getResourceAsStream(PLAYER_SPRITE_PATH));

        // Start player
        player = new Player (5, 4); // start tile, adapt after meeting
        player.setSpeedTilesPerSecond(4.0);
        player.setFrameDurationNs(120); // animation step (ms)

        // Erst rendern, wenn Layout fertig ist (Canvas ist sonst oft 0x0)
        Platform.runLater(this::render);

        // Bei Resize neu rendern (wir machen kein Game-Loop, sondern "on demand")
        gameCanvas.widthProperty().addListener((obs, oldV, newV) -> render());
        gameCanvas.heightProperty().addListener((obs, oldV, newV) -> render());
    }

    // Scene based init: key handling + game loop
    public void init(Scene scene) {
        scene.setOnKeyPressed(e -> keys.put(e.getCode(), true));
        scene.setOnKeyReleased(e -> keys.put(e.getCode(), false));

        root.setOnMouseClicked(e -> root.requestFocus());
        Platform.runLater(() -> root.requestFocus());

        // Game loop
        loop = new AnimationTimer() {
            long last = 0;

            @Override
            public void handle(long now) {
                if (last == 0) { last = now; return; }
                double dt  = (now - last) / 1_000_000_000.0;
                last = now;

                //Eingabe
                boolean up = isDown(KeyCode.W);
                boolean down = isDown(KeyCode.S);
                boolean left = isDown(KeyCode.A);
                boolean right = isDown(KeyCode.D);

                // Movement grid + animation
                player.update(dt, up, down, left, right);
                player.animate(now, player.isMoving());

                // Draw
                render();
            }
        };
        loop.start();
    }

    private boolean isDown(KeyCode code) { return keys.getOrDefault(code, false); }

    /**
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

    /**
     * Findet einen Layer anhand des Namens und rendert ihn, wenn es ein TileLayer ist.
     */
    private void renderLayerByName(GraphicsContext gc, String layerName, int firstGid, int columns) {
        for (var layer : map.layers()) {
            if (layer.isTileLayer() && layerName.equals(layer.name())) {
                renderer.renderTileLayer(gc, map, layer, tilesetImage, firstGid, columns);
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
        if (playerSprite == null || map == null) return;

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

        double tileX = player.getTileX();
        double tileY = player.getTileY();
        int tilePxX = (int) Math.round(tileX * scaledTileW);
        int tilePxY = (int) Math.round(tileY * scaledTileH);

//        // Tile top-left in Pixel
//        int tilePxX = baseX + player.getTileX() * scaledTileW;
//        int tilePxY = baseY + player.getTileY() * scaledTileH;

        // Zentrum im Tile
        int dx = tilePxX + (scaledTileW - targetW) / 2;
        int dy = tilePxY + (scaledTileH - targetH) / 2;

        // Optischer Anchor nach oben (gegen "bottom-heavy" Eindruck - "zentriert")
        dy -= (int) Math.round(scaledTileH * PLAYER_Y_ANCHOR);

        gc.drawImage(playerSprite, sx, sy, frameW, frameH, dx, dy, targetW, targetH);
    }

    public void stop() { if (loop != null) { loop.stop(); } }

}
