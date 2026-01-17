package at.ac.hcw.kanuescape.controller;

import at.ac.hcw.kanuescape.game.Player; // Mvm
import at.ac.hcw.kanuescape.game.dialogue.dialogueManager; //dialogue
import at.ac.hcw.kanuescape.tiled.*;

import at.ac.hcw.kanuescape.ui.DialogueBox;
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
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.input.KeyCode; // Mvm

import java.util.EnumMap; // Mvm
import java.util.Map; // Mvm

/**
 * GameController
 * <p>
 * - Lädt Map + Tileset + Player-Sprite aus /resources
 * - Bindet Canvas an das Fenster (resizable) mit Rahmen (Padding)
 * - Rendert Map-Layer + Player (noch ohne Bewegung)
 * <p>
 * Hinweis: Parsing & Tileset-Logik steckt in MapLoader/TiledModel.
 * Rendering der TileLayer steckt in MapRenderer.
 */

public class GameController {

    // Ressourcenpfade
    private static final String MAP_PATH = "/assets/maps/game_screen.json";
    private static final String TSX_PATH = "/assets/tileset/game_screen.tsx";
    private static final String TILESET_IMAGE_PATH = "/assets/images/tileset/tileset.png";
    private static final String PLAYER_SPRITE_PATH = "/assets/images/sprite/nerdyguy_sprite.png";

    // Look & Layout
    private static final Color BACKGROUND = Color.web("#4e4e4e");
    private static final double FRAME_PADDING = 25; // muss zum FXML -fx-padding passen

    private Player player;

    // "Optischer" Offset: Sprites sonst nicht "zentriert" im Tile
    private static final double PLAYER_Y_ANCHOR = 0.10;

    // Engine-Teile
    private final MapRenderer renderer = new MapRenderer();

    // Map als Feld speichern, damit render() später damit arbeiten kann
    private TiledModel.TiledMap map;
    private TiledModel.TsxTileset tsx;
    private Image tilesetImage;

    // Interactions über Interactions Layer
    private TiledModel.TiledLayer interactionsObjectLayer;

    // Player sprite
    private Image playerSprite;

    //to DoListe
    private Stage todoStage;
    private Stage BuecherStage;
    private Stage LaptopStage;
    private ToDoListeController todoController;
    private BuecherController BuecherController;
    private LaptopController LaptopController;

    //render Context für größen
    private RenderContext renderContext;

    public RenderContext rc;
    //Die Objekt Layer wird hier gespeichert
    //   private TiledModel.TiledLayer interactionLayer;
    private TiledModel.TiledLayer collisionLayer;



    //Dialogue Text
    private final dialogueManager dialogueManager = new dialogueManager();
    private DialogueBox dialogueBox;


    @FXML
    private StackPane root;
    @FXML
    private Canvas gameCanvas;


    @FXML
    private void initialize() throws Exception {

        // Canvas folgt der Größe des Containers, bleibt aber innen "kleiner" (Rahmen bleibt sichtbar)
        gameCanvas.widthProperty().bind(root.widthProperty().subtract(FRAME_PADDING * 2));
        gameCanvas.heightProperty().bind(root.heightProperty().subtract(FRAME_PADDING * 2));

        // Ressourcen laden (ohne UI-Logik)
        map = MapLoader.loadMap(MAP_PATH);
        tsx = MapLoader.loadTsxTileset(TSX_PATH);
        tilesetImage = MapLoader.loadImage(TILESET_IMAGE_PATH);
        playerSprite = new Image(getClass().getResourceAsStream(PLAYER_SPRITE_PATH));

        // Interactions
        interactionsObjectLayer = findLayer("interactions");

        // Erst rendern, wenn Layout fertig ist (Canvas ist sonst oft 0x0)
        Platform.runLater(this::render);

        // Bei Resize neu rendern (wir machen kein Game-Loop, sondern "on demand")
        gameCanvas.widthProperty().addListener((obs, oldV, newV) -> render());
        gameCanvas.heightProperty().addListener((obs, oldV, newV) -> render());

        dialogueBox = new DialogueBox();
        dialogueBox.bindToRoot(root);
        root.getChildren().add(dialogueBox);   // ganz oben im Stack


        // Fehler bei Einfügen
        // Mvm; initialize player; start position for now (5,4)
        player = new Player(3, 4);
        player.setSpeedTilesPerSecond(4.0);
        player.setFrameDurationMs(120); // ms; per animation step, might have to change after test

        // ToDoListe Laden
        FXMLLoader fxmlLoader = new FXMLLoader(GameController.class.getResource("/fxml/toDoListe.fxml"));
        Scene scenetodo = new Scene(fxmlLoader.load(), 339, 511);
        todoController = fxmlLoader.getController();
        scenetodo.setFill(Color.TRANSPARENT);
        todoStage = new Stage();
        todoStage.setAlwaysOnTop(true);
        todoStage.setResizable(false);
        todoStage.initStyle(StageStyle.TRANSPARENT);
        todoStage.setY(100);
        todoStage.setScene(scenetodo);

        FXMLLoader fxmlLoaderBuecher = new FXMLLoader(GameController.class.getResource("/fxml/Buecher.fxml"));
        Scene sceneBuecher = new Scene(fxmlLoaderBuecher.load(), 339, 511);
        BuecherController = fxmlLoaderBuecher.getController();
        sceneBuecher.setFill(Color.TRANSPARENT);
        BuecherStage = new Stage();
        BuecherStage.setAlwaysOnTop(true);
        BuecherStage.setResizable(false);
        BuecherStage.initStyle(StageStyle.TRANSPARENT);
        BuecherStage.setY(100);
        BuecherStage.setScene(sceneBuecher);

        FXMLLoader fxmlLoaderLaptop = new FXMLLoader(GameController.class.getResource("/fxml/Laptop.fxml"));
        Scene sceneLaptop = new Scene(fxmlLoaderLaptop.load(), 339, 511);
        LaptopController = fxmlLoaderLaptop.getController();
        sceneLaptop.setFill(Color.TRANSPARENT);
        LaptopStage = new Stage();
        LaptopStage.setAlwaysOnTop(true);
        LaptopStage.setResizable(false);
        LaptopStage.initStyle(StageStyle.TRANSPARENT);
        LaptopStage.setY(100);
        LaptopStage.setScene(sceneLaptop);


        gameCanvas.setOnMouseClicked(e -> handleInteractionClick(e.getX(), e.getY()));
    }

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
        renderLayerByName(gc, "collision", firstGid, columns);

        // Player darüber zeichnen
        renderPlayer(gc);
    }


    /**
     * Findet einen Layer anhand des Namens und rendert ihn, wenn es ein TileLayer ist.
     */
    private void renderLayerByName(GraphicsContext gc, String layerName, int firstGid, int columns) {
        for (var layer : map.layers()) {
            if (layer.isTileLayer() && layerName.equals(layer.name())) {
                rc = renderer.renderTileLayer(gc, map, layer, tilesetImage, firstGid, columns);

                // Wir merken uns EINEN Layer für Interaktionen
//                if ("objects".equals(layerName)) {
//                    renderContext = rc;
//                    interactionLayer = layer;
//                }

                if ("collision".equals(layerName)) {
                    renderContext = rc;
                    collisionLayer = layer; // Hier sagen wir dem Programm: Das ist die Ebene mit den Wänden!
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

//        gameCanvas.setOnMouseClicked(e ->
//                handleInteractionClick(e.getX(), e.getY()));
    }

    // Mvm
    private final Map<KeyCode, Boolean> keys = new EnumMap<>(KeyCode.class);
    private AnimationTimer loop;

    // Mvm
    public void init(Scene scene) {
        render();

        // Key states (WASD)
        scene.setOnKeyPressed(e -> keys.put(e.getCode(), true));
        scene.setOnKeyReleased(e -> keys.put(e.getCode(), false));

        // Focus: ensure that key events arrive
        root.setOnMouseClicked(e -> root.requestFocus());
        root.requestFocus();

        // Game-loop: requests render() on every frame
        loop = new AnimationTimer() {
            long last = 0;

            @Override
            public void handle(long now) {
                if (last == 0) {
                    last = now;
                    return;
                }


                //sperrt movement während text box offen ist
                if (dialogueBox != null && dialogueBox.isVisible()) {
                    player.animate(now, false); // idle animation
                    render();
                    return;
                }


                double dt = (now - last) / 1_000_000_000.0; // time delta in seconds
                last = now;

                boolean up = isDown(KeyCode.W);
                boolean down = isDown(KeyCode.S);
                boolean left = isDown(KeyCode.A);
                boolean right = isDown(KeyCode.D);

                double dx = 0, dy = 0;
                if (up) dy = -1;
                else if (down) dy = 1;
                else if (left) dx = -1;
                else if (right) dx = 1;


//                double nextX = player.tileX + dx;
//                double nextY = player.tileY; // Y unverändert
//                if (!isTileBlocked(nextX, nextY)) {
//                    player.update(dt / 2, up, down, left, right);
//                    // Animation (idle vs moving)
//                    player.animate(now, up || down || left || right);
//                    render();
//                }
//
//                nextX = player.tileX; // X evtl. schon angepasst
//                nextY = player.tileY + dy;
//                if (!isTileBlocked(nextX, nextY)) {
//                    player.update(dt / 2, up, down, left, right);
//                    // Animation (idle vs moving)
//                    player.animate(now, up || down || left || right);
//                    render();
//                }
//                nextX = player.tileX;
//                nextY = player.tileY;
//                if (isTileBlocked(nextX, nextY)) {
//                    // Animation (idle vs moving)
//                    player.animate(now, false);
//                    render();
//                }

                double step = 4.0 * dt; // oder: player.getSpeedTilesPerSecond() * dt

                boolean moved = false;

                if (dx != 0) {
                    double nextX = player.tileX + dx * step;
                    double nextY = player.tileY;

                    if (!isTileBlocked(nextX, nextY)) {
                        player.update(dt, up, down, left, right);
                        moved = true;
                    } else {
                        maybeShowExitBathText(nextX, nextY);
                    }
                }

                if (dy != 0) {
                    double nextX = player.tileX;
                    double nextY = player.tileY + dy * step;

                    if (!isTileBlocked(nextX, nextY)) {
                        player.update(dt, up, down, left, right);
                        moved = true;
                    } else {
                        maybeShowExitBathText(nextX, nextY);
                    }
                }

                player.animate(now, moved);
                render();


            }
        };
        loop.start();
    }

    private boolean isDown(KeyCode code) {
        return keys.getOrDefault(code, false);
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

    private boolean isTileBlocked(double nextX, double nextY) {
        // Sicherheitsabfrage: Wenn der Collision-Layer nicht geladen wurde, erlauben wir die Bewegung (verhindert Absturz).
        if (collisionLayer == null) return false;

        double playerWidthTiles = 0.9;
        double playerHeightTiles = 0.9;

        // Tiles die vom Spieler überlappt werden
        int startX = (int) Math.floor(nextX);
        int endX = (int) Math.floor(nextX + playerWidthTiles - 0.001);
        int startY = (int) Math.floor(nextY);
        int endY = (int) Math.floor(nextY + playerHeightTiles - 0.001);
        if (11 <= nextY + playerHeightTiles - 0.001 && nextY + playerHeightTiles - 0.001 <= 12.4) {
            endY = 11;
        }


        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                if (x < 0 || y < 0 || x >= collisionLayer.width() || y >= collisionLayer.height())
                    return true;

                int index = y * collisionLayer.width() + x;

                int gid = collisionLayer.data()[index];
                System.out.println(nextX + playerHeightTiles - 0.001 + " " + endX);
                System.out.println(y * collisionLayer.width() + " " + x);
                System.out.println(index + " " + gid);
                if (gid == 91) return true; // Wand
            }
        }
        return false;
    }


    // Layer aus der Tiled-Map anhand des Namens finden
    private TiledModel.TiledLayer findLayer(String name) {
        if (map == null) return null;
        for (var layer : map.layers()) {
            if (name.equals(layer.name())) return layer;
        }
        return null;
    }

    // Interactions on click with the interactions layer from json
    private void handleInteractionClick(double mouseX, double mouseY) {
        //wenn textbox offen: klick schließt sie (is auch nochmal im overlay), Canvas-Clicks ignorieren
        if (dialogueBox != null && dialogueBox.isVisible()) {
            return;
        }

        // wenn was noch nicht gerendert oder kompiliert wurde, brichts ab
        if (renderContext == null || interactionsObjectLayer == null || interactionsObjectLayer.objects() == null) {
            return;
        }

        /*
        damit interactions laxer an scale der map angepasst wird
         */
        MapTransform t = computeMapTransform();

        // map tatsächlich in pixel
        double mapPixelW = map.width() * map.tilewidth();       // 20 * 32
        double mapPixelH = map.height() * map.tileheight();     // 15 * 32

        // position des klicks in relation zur scale der map
        double localX = ((mouseX - t.baseX()) / t.renderW()) * mapPixelW;
        double localY = ((mouseY - t.baseY()) / t.renderH()) * mapPixelH;

        //außerhalb der map
        if (localX < 0 || localY < 0) {
            return;
        }

        //findet erstes object, dessen rectangle den klick enthält
        for (var obj : interactionsObjectLayer.objects()) {
            if (pointInRect(localX, localY, obj.x(), obj.y(), obj.width(), obj.height())) {

                String type = obj.propString("type");
                if (type != null) {
                    String ttt = type.toLowerCase();
                    if (ttt.equals("exit") || ttt.equals("door_exit") || ttt.equals("bath") || ttt.equals("bathroom") || ttt.equals("toilet")) {
                        return; // nicht klickbar
                    }
                }

                //text wenn zu weit weg
//                if (!isPlayerNearObject(obj)) {
//                    if (dialogueBox != null) dialogueBox.show("You're too far away.");
//                    return;
//                }

                //kein text wenn zu weit weg
                if (!isPlayerNearObject(obj)) {
                    return; // zu weit weg → nichts passiert
                }


                onInteractionObjectClicked(obj);
                return;
            }
        }


    }

    // wo klick man in object interaction rectangle -> p = punkt, r = rectangle
    private boolean pointInRect(double px, double py, double rx, double ry, double rw, double rh) {
        return px >= rx && px <= (rx + rw) && py >= ry && py <= (ry + rh);
    }

    // wenn dann objekt geklickt wird
    private void onInteractionObjectClicked(TiledModel.TiledObject obj) {
        String type = obj.propString("type");
        if (type == null) type = "unknown";

        // Exit/Bath NICHT klickbar
        if (type.equals("exit") || type.equals("bathroom")) {
            return;
        }


        // PUZZLES: kein Text, eigene Logik
        switch (type) {
            case "bookcase" -> {
                if (BuecherStage != null) BuecherStage.show();
                return;
            }
            case "laptop" -> {
                if (LaptopStage != null) LaptopStage.show();
                return;
            }
            case "fridge" -> {
                if (todoStage != null) todoStage.show();
                return;
            }
            case "stove" -> {
                // TODO: coocking puzzle
                return;
            }
            case "ascii" -> {
                //TODO ascii tabelle
                return;
            }
        }

        // Alles andere: Text anzeigen
        String text = dialogueManager.nextTextForType(type);
        if (dialogueBox != null) {
            dialogueBox.show(text);
        }

    }

    // hilfmethoden damit object interaction rectangles an scale richtig angepasst werden
    private record MapTransform(int baseX,          // linker Rand der Map auf dem Canvas
                                int baseY,          // oberer Rand der Map auf dem Canvas
                                double renderW,     // gerenderte Breite der Map (nach Skalierung)
                                double renderH      // gerenderte Höhe der Map
    ) {
    }

    private MapTransform computeMapTransform() {
        int tileW = map.tilewidth();
        int tileH = map.tileheight();

        double canvasW = gameCanvas.getWidth();     // 20
        double canvasH = gameCanvas.getHeight();    // 15

        // auf pixel gerechnet. *32
        double mapW = map.width() * tileW;
        double mapH = map.height() * tileH;

        double scale = Math.min(canvasW / mapW, canvasH / mapH);

        // weil die map nicht eins zu eins sondern gescaled ist
        double renderW = mapW * scale;
        double renderH = mapH * scale;

        int baseX = (int) Math.round((canvasW - renderW) / 2.0);
        int baseY = (int) Math.round((canvasH - renderH) / 2.0);

        return new MapTransform(baseX, baseY, renderW, renderH);
    }


    private void maybeShowExitBathText(double nextTileX, double nextTileY) {
        if (dialogueBox == null || dialogueBox.isVisible()) return;
        if (interactionsObjectLayer == null || interactionsObjectLayer.objects() == null) return;
        if (map == null) return;

        // wir nehmen den CENTER-point der "gewollten" Position
        double cx = (nextTileX + 0.5) * map.tilewidth();
        double cy = (nextTileY + 0.5) * map.tileheight();

        for (var obj : interactionsObjectLayer.objects()) {
            String type = obj.propString("type");
            if (type == null) continue;

            String t = type.toLowerCase();
            boolean isExitOrBath =
                    t.equals("exit") || t.equals("door_exit") ||
                            t.equals("bath") || t.equals("bathroom") || t.equals("toilet");
            if (!isExitOrBath) continue;

            boolean inside = cx >= obj.x() && cx <= obj.x() + obj.width()
                    && cy >= obj.y() && cy <= obj.y() + obj.height();
            if (inside) {
                dialogueBox.show(dialogueManager.nextTextForType(type));
                return;
            }
        }
    }

    private boolean isPlayerNearObject(TiledModel.TiledObject obj) {
        if (map == null || player == null) return false;

        int tw = map.tilewidth();
        int th = map.tileheight();

        //range für object interaction
        int range = 2;

        // Objekt-Grenzen in TILE-Koordinaten (double!)
        double objLeft = obj.x() / tw;
        double objTop = obj.y() / th;
        double objRight = (obj.x() + obj.width()) / tw;
        double objBottom = (obj.y() + obj.height()) / th;

        // Player-Position in TILE-Koordinaten (double!)
        double px = player.getTileX();
        double py = player.getTileY();

        // 4-directional adjacency:
        // links vom Objekt, rechts vom Objekt, über dem Objekt, unter dem Objekt
        boolean left = px >= objLeft - range && px <= objLeft
                && py >= objTop && py <= objBottom;

        boolean right = px <= objRight + range && px >= objRight
                && py >= objTop && py <= objBottom;

        boolean up = py >= objTop - range && py <= objTop
                && px >= objLeft && px <= objRight;

        boolean down = py <= objBottom + range && py >= objBottom
                && px >= objLeft && px <= objRight;

        // 4 diagonalen
        boolean diagUL = px == objLeft - 1 && py == objTop - 1;
        boolean diagUR = px == objRight + 1 && py == objTop - 1;
        boolean diagDL = px == objLeft - 1 && py == objBottom + 1;
        boolean diagDR = px == objRight + 1 && py == objBottom + 1;

        return left || right || up || down || diagUL || diagUR || diagDL || diagDR;
    }

}
