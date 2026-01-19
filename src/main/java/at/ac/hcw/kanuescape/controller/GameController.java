package at.ac.hcw.kanuescape.controller;

import at.ac.hcw.kanuescape.controller.BuecherController;
import at.ac.hcw.kanuescape.controller.LaptopController;
import at.ac.hcw.kanuescape.controller.ToDoListeController;
import at.ac.hcw.kanuescape.controller.ui.DialogueBoxController;
import at.ac.hcw.kanuescape.game.dialogue.DialogueManager;
import at.ac.hcw.kanuescape.game.KochManager;
import at.ac.hcw.kanuescape.tiled.MapLoader;
import at.ac.hcw.kanuescape.tiled.MapRenderer;
import at.ac.hcw.kanuescape.tiled.RenderContext;
import at.ac.hcw.kanuescape.tiled.TiledModel;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import at.ac.hcw.kanuescape.game.Player; // Mvm
import javafx.animation.AnimationTimer; // Mvm
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.input.KeyCode; // Mvm
import javafx.util.Duration;

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
    @FXML private StackPane gameArea;
    private static final double FRAME_PADDING = 10; // muss zum FXML -fx-padding passen


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

    //to DoListe
    private Stage todoStage;
    private Stage BuecherStage;
    private Stage LaptopStage;
    private Stage SchrankStage;
    private Stage KuehlschrankStage;
    private ToDoListeController todoController;
    private BuecherController BuecherController;
    private LaptopController LaptopController;
    private SchrankController SchrankController;
    private KuehlschrankController KuehlschrankController;

    private KochManager KochManager = new KochManager();

    // Render Context für Größen
    private RenderContext renderContext;

    public RenderContext rc;
    // Die Objekt Layer wird hier gespeichert
    private TiledModel.TiledLayer interactionLayer;
    private TiledModel.TiledLayer interactionLayer2;
    private TiledModel.TiledLayer collisionLayer;

    @FXML
    private StackPane root;
    @FXML
    private Canvas gameCanvas;
    @FXML
    private ImageView menuBtn;
    @FXML
    private Label menuLabel;

    @FXML
    private StackPane overlayLayer;

    private StackPane dialogueNode;  // Node merken, um es zu entfernen
    private boolean dialogueOpen = false;
    private final DialogueManager dialogueManager = new DialogueManager();


    private DialogueBoxController dialogueController;

    // Input/loop
    private final Map<KeyCode, Boolean> keys = new EnumMap(KeyCode.class);
    private AnimationTimer loop;

    @FXML
    private void initialize() throws Exception {

        // Canvas folgt der Größe des Containers, bleibt aber innen "kleiner" (Rahmen bleibt sichtbar)
        gameCanvas.widthProperty().bind(gameArea.widthProperty());
        gameCanvas.heightProperty().bind(gameArea.heightProperty());



        // Ressourcen laden (ohne UI-Logik)
        map = MapLoader.loadMap(MAP_PATH);
        tsx = MapLoader.loadTsxTileset(TSX_PATH);
        tilesetImage = MapLoader.loadImage(TILESET_IMAGE_PATH);
        playerSprite = new Image(getClass().getResourceAsStream(PLAYER_SPRITE_PATH));

        // Start player
        player = new Player(5, 4); // start tile
        player.setSpeedTilesPerSecond(4.0);
        player.setFrameDurationNs(120); // animation step (ms)

        // Erst rendern, wenn Layout fertig ist (Canvas ist sonst oft 0x0)
        Platform.runLater(this::render);



        // --- DialogueBox ---
        loadDialogueBox();


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
        Scene sceneBuecher = new Scene(fxmlLoaderBuecher.load());
        BuecherController = fxmlLoaderBuecher.getController();
        sceneBuecher.setFill(Color.BLACK);
        BuecherStage = new Stage();
        BuecherStage.setAlwaysOnTop(true);
        BuecherStage.setResizable(false);
        BuecherStage.initStyle(StageStyle.TRANSPARENT);
        BuecherStage.setScene(sceneBuecher);

        FXMLLoader fxmlLoaderSchrank = new FXMLLoader(GameController.class.getResource("/fxml/Schrank.fxml"));
        Scene sceneSchrank = new Scene(fxmlLoaderSchrank.load());
        SchrankController = fxmlLoaderSchrank.getController();
        sceneSchrank.setFill(Color.BLACK);
        SchrankStage = new Stage();
        SchrankStage.setAlwaysOnTop(true);
        SchrankStage.setResizable(false);
        SchrankStage.initStyle(StageStyle.TRANSPARENT);
        SchrankStage.setScene(sceneSchrank);


        FXMLLoader fxmlLoaderKuehlschrank = new FXMLLoader(GameController.class.getResource("/fxml/Kuehlschrank.fxml"));
        Scene sceneKuehlschrank = new Scene(fxmlLoaderKuehlschrank.load());
        KuehlschrankController = fxmlLoaderKuehlschrank.getController();
        sceneKuehlschrank.setFill(Color.BLACK);
        KuehlschrankStage = new Stage();
        KuehlschrankStage.setAlwaysOnTop(true);
        KuehlschrankStage.setResizable(false);
        KuehlschrankStage.initStyle(StageStyle.TRANSPARENT);
        KuehlschrankStage.setScene(sceneKuehlschrank);

        // ! Adapted from here ...

        FXMLLoader fxmlLoaderLaptop = new FXMLLoader(GameController.class.getResource("/fxml/Laptop.fxml"));
        Scene sceneLaptop = new Scene(fxmlLoaderLaptop.load(), 977, 824);
        LaptopController = fxmlLoaderLaptop.getController();

        LaptopStage = new Stage();
        LaptopStage.setTitle("Programming");
        LaptopStage.setAlwaysOnTop(true);
        LaptopStage.setResizable(false);

        // for now/until debugged: regular window (non-transparent)
        LaptopStage.initStyle(StageStyle.DECORATED);

        LaptopStage.setScene(sceneLaptop);
        LaptopStage.sizeToScene();

        // Closing window via ESC
        sceneLaptop.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                LaptopStage.close();
                e.consume();
            }
        });

        // Siegbedingung checked upon closing of laptop-window
        LaptopStage.setOnHiding(e -> {
            if (LaptopController != null && LaptopController.isSolved()) {
                CheckProg(); // use to check to-do?
            }
        });

//        // NEW: for checking result -- delete? Check-button no longer in use
//        LaptopController.setOnSolved(() -> {
//        CheckProg();
//        });

        // ! Adaptions until here!

        // Resets the visual effects on the main root node when the book stage is closed
        BuecherStage.setOnHiding(event -> {
            // Remove the blur or any other effect from the background immediately upon hiding
            root.setEffect(null);
        });
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

                //no mvmt when text box open
                if (dialogueOpen) {
                    player.animate(now, false);
                    render();
                    return;
                }


                if (last == 0) {
                    last = now;
                    return;
                }
                double dt = (now - last) / 1_000_000_000.0;
                last = now;

                //Eingabe
                boolean up = isDown(KeyCode.W);
                boolean down = isDown(KeyCode.S);
                boolean left = isDown(KeyCode.A);
                boolean right = isDown(KeyCode.D);


                if (!player.isMoving()) {
                    if (!isTileBlocked(player.getGridX(), player.getGridY(), up, down, left, right)) {
                        player.update(dt, up, down, left, right);
                    }
                } else {
                    player.update(dt, up, down, left, right);
                }


                player.animate(now, player.isMoving());
                render();


            }
        };
        loop.start();

        gameCanvas.setOnMouseClicked(e -> handleMapClick(e.getX(), e.getY()));

    }

    private boolean isDown(KeyCode code) {
        return keys.getOrDefault(code, false);
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
                if ("objects".equals(layerName)) {
                    renderContext = rc;
                    interactionLayer = layer;
                }
                if ("objects_front".equals(layerName)) {
                    renderContext = rc;
                    interactionLayer2 = layer;
                }
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

        double tileX = player.getTileX();
        double tileY = player.getTileY();
        int tilePxX = (int) Math.round(tileX * scaledTileW);
        int tilePxY = (int) Math.round(tileY * scaledTileH);

        // Centered in tile
        int dx = baseX + tilePxX + (scaledTileW - targetW) / 2;
        int dy = baseY + tilePxY + (scaledTileH - targetH) / 2;

        // Optical anchor towards top (against "bottom-heavy" impression - "centered")
        dy -= (int) Math.round(scaledTileH * PLAYER_Y_ANCHOR);

        // Drawing (source: sx, sy, frameW, frameH -> destination: dx, dy, targetW, targetH)
        gc.drawImage(playerSprite, sx, sy, frameW, frameH, dx, dy, targetW, targetH);

//        gameCanvas.setOnMouseClicked(e -> {
//            handleMapClick(e.getX(), e.getY());
//        });
    }

    public void stop() {
        if (loop != null) {
            loop.stop();
        }
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
        int gid2 = interactionLayer2.data()[index];

        // nimm das "oberste" / relevante gid
        int clickedGid = (gid2 != 0) ? gid2 : gid;

        onTileClicked(tileX, tileY, clickedGid);
    }

    private void onTileClicked(double x, double y, int gid) {
        if (gid == 0) return;

        double tileX = player.getGridX();
        double tileY = player.getGridY();

            System.out.println("Tile geklickt: (" + x*32 + "," + y*32 + ") GID=" + gid);
            System.out.println("Tile geklickt: (" + x + "," + y);
            System.out.println("Player geklickt: (" + tileX + "," + tileY );

        if (Math.abs(tileX - x) <= 2 && Math.abs(tileY - y) <= 2) {

            if (gid == 60) {
                if (todoStage != null) {
                    todoStage.setX(rc.renderW() / 2);
                    todoStage.show();
                }
            }
            if (gid == 55||gid==56) {
                if (SchrankStage != null) {
                    SchrankStage.setX(rc.renderW()/2*1.2);
                    SchrankStage.setY(rc.renderH()/2);
                    SchrankStage.show();
                }
                String Schrank = KochManager.cabinet();
                System.out.println(Schrank);
            }
            if (gid == 58||gid==70) {//linkerSchrank
                String linkerSchrank = KochManager.shelf();
                System.out.println(linkerSchrank
                );
            }
            if (gid == 71||gid==59) {//Herd
                String Herd = KochManager.stove();
                System.out.println(Herd);
            }
            if (gid == 63) {//Schneidebrett
                String Schneidebrett = KochManager.board();
                System.out.println(Schneidebrett);
                if(KochManager.getState()== KochManager.getFINISHED()){
                    CheckKochen();
                }
            }
            if (gid == 64) {//Wasserhahn
                String Wasser = KochManager.water();
                System.out.println(Wasser);
                System.out.println(KochManager.getState());
            }
            if (gid == 72) {
                if (KuehlschrankStage != null) {
                    KuehlschrankStage.setX(rc.renderW() / 2);
                    KuehlschrankStage.show();
                }
                String Kuehlschrank = KochManager.fridge();
                System.out.println(Kuehlschrank);
            }
            if (gid == 94 || gid == 93 || gid == 82 || gid == 81) {
                // Check if the book stage is initialized before displaying it
                if (BuecherStage != null) {
                    // Apply a blur effect to the background root to focus the user's attention on the new window
                    root.setEffect(new GaussianBlur(20));

                    // Display the stage and set its specific screen coordinates
                    BuecherStage.show();
                    BuecherStage.setX(470);
                    BuecherStage.setY(210);

                    // Retrieve the root node of the stage's scene to apply animations
                    Parent content = BuecherStage.getScene().getRoot();

                    // Create and play a smooth fade-in transition for the stage content
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(500), content);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();

                    // Ensure the controller is available to manage focus
                    if (BuecherController != null) {
                        // Request focus on the main pane of the sub-scene to enable keyboard interactions immediately
                        BuecherController.getBuecherScene().requestFocus();
                    }
                }
            }
            if (gid == 50) {
                if (LaptopStage != null) {
                    LaptopStage.setX(rc.renderW() / 2);
                    LaptopStage.show();
                }
            }
//            //Kiki
//            if (gid == 66) {
//                //sachen rein schreiben (Ali Code hier)
//            }

            // Dialogue fenster + Text
            String type = dialogueManager.typeForGid(gid);
            if (type != null) {

                if ("picture".equals(type)) {
                    dialogueManager.resetPicture(); // immer bei Zeile 1 starten
                    openDialogue(dialogueManager.nextPictureLine(), "picture");
                } else {
                    openDialogue(dialogueManager.nextTextForType(type), type);
                }
                return;
            }
        }
    }
    boolean Buecher = false;
    boolean Kochen = false;
    boolean Mathe = false;
    boolean Prog = false;

    public void CheckBuecher() {
        if (todoController != null) {
            todoController.CheckBuecher(true);
            Buecher = true;
        }
    }
    @FXML
    public void CheckKochen() {
        if (todoController != null) {
            todoController.CheckKochen(true);
            Kochen = true;
        }
    }
    @FXML
    public void CheckMathe() {
        if (todoController != null) {
            todoController.CheckMathe(true);
            Mathe = true;
        }
    }
    @FXML
    public void CheckProg() {
        if (todoController != null) {
            todoController.CheckProg(true);
            Prog = true;
        }
    }

    public void Win() {
        if (Prog=true&&Mathe==true&&Kochen==true&&Buecher==true) {
            System.out.println("Win");
        }
    }

    private boolean isTileBlocked(int nextGridX, int nextGridY, boolean up, boolean down, boolean left, boolean right) {
        // Safety check: Layer oder RenderContext nicht verfügbar
        if (collisionLayer == null || renderContext == null) return false;

        // Prüfe, ob die Koordinaten außerhalb der Map liegen
        if (nextGridX < 0 || nextGridY < 0 ||
                nextGridX >= collisionLayer.width() || nextGridY >= collisionLayer.height()) {
            return true; // außerhalb = blockiert
        }
        int index;
        if(up){
            index = (nextGridY-1) * 20 + nextGridX;
        } else if (down) {
            index = (nextGridY+1) * 20 + nextGridX;
        } else if (left) {
            index = nextGridY * 20 + nextGridX-1;
        } else if (right) {
            index = nextGridY * 20 + nextGridX+1;
        }
        else{
            return false;
        }

        System.out.println(index);
        // Berechne den Index im Layer
//        int index = nextGridY * 20 + nextGridX;
        int gid = collisionLayer.data()[index];
        System.out.println(player.getGridX()+" "+ player.getGridY());

        // GID 0 = kein Hindernis, alles andere = blockiert
        return gid != 0;
    }


    private void loadDialogueBox() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ui/DialogueBox.fxml"));
            dialogueNode = loader.load();
            dialogueController = loader.getController();

            // nur einmal einhängen
            overlayLayer.getChildren().setAll(dialogueNode);

            // ABER: nicht anzeigen beim Start
            overlayLayer.setVisible(false);
            overlayLayer.setManaged(false);
            dialogueOpen = false;

            // Klick schließt (Handler ist ok, wirkt nur wenn sichtbar)
            overlayLayer.setOnMouseClicked(e -> {
                if ("picture".equals(activeDialogueType)) {

                    if (dialogueManager.isPictureFinished()) {
                        dialogueManager.resetPicture();
                        closeDialogue();
                    } else {
                        openDialogue(dialogueManager.nextPictureLine(), "picture");
                    }

                    e.consume();
                    return;
                }

                closeDialogue();
            });



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String activeDialogueType = null;
    public void openDialogue(String text) {
        openDialogue(text, null);
    }

    public void openDialogue(String text, String type) {
        if (dialogueController == null) return;

        activeDialogueType = type;
        dialogueController.setText(text);

        overlayLayer.setVisible(true);
        overlayLayer.setManaged(true);
        dialogueOpen = true;
    }

    public void closeDialogue() {
        overlayLayer.setVisible(false);
        overlayLayer.setManaged(false);
        dialogueOpen = false;
        activeDialogueType = null;
    }

}
