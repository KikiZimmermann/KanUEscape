package at.ac.hcw.kanuescape.controller;

import at.ac.hcw.kanuescape.controller.BuecherController;
import at.ac.hcw.kanuescape.controller.LaptopController;
import at.ac.hcw.kanuescape.controller.ToDoListeController;
import at.ac.hcw.kanuescape.controller.MathController;
import at.ac.hcw.kanuescape.controller.ui.DialogueBoxController;
import at.ac.hcw.kanuescape.game.dialogue.DialogueManager;
import at.ac.hcw.kanuescape.game.KochManager;
import at.ac.hcw.kanuescape.game.dialogue.DialogueTexts;
import at.ac.hcw.kanuescape.tiled.MapLoader;
import at.ac.hcw.kanuescape.tiled.MapRenderer;
import at.ac.hcw.kanuescape.tiled.RenderContext;
import at.ac.hcw.kanuescape.tiled.TiledModel;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import at.ac.hcw.kanuescape.game.Player; // Mvm
import javafx.animation.AnimationTimer; // Mvm
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.input.KeyCode; // Mvm
import javafx.util.Duration;
import at.ac.hcw.kanuescape.audio.AudioManager;
import at.ac.hcw.kanuescape.audio.AudioPaths;

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
    private static final String TSX_PATH = "/assets/tilesets/game_screen.tsx";
    private static final String TILESET_IMAGE_PATH = "/assets/images/tileset/tileset.png";
    private static final String PLAYER_SPRITE_PATH = "/assets/images/sprite/nerdyguy_sprite.png";

    // Look & Layout
    private static final Color BACKGROUND = Color.web("#4e4e4e");
    @FXML
    private StackPane gameArea;

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

    // GameIntro
    private boolean introRunning = false;
    private int introIndex = 0;
    // GameEnd
    private boolean endRunning = false;
    private int endIndex = 0;

    //SFX-wall bump
    private long lastBumpSfxNs = 0;
    private static final long BUMP_COOLDOWN_NS = 250_000_000L; // 250ms
    //SFX - hover
    private long lastHoverSfxNs = 0;
    private static final long HOVER_COOLDOWN_NS = 120_000_000L; // 120ms

    //to DoListe
    private Stage todoStage;
    private Stage BuecherStage;
    private Stage LaptopStage;
    private Stage SchrankStage;
    private Stage KuehlschrankStage;
    private Stage MathStage;

    private ToDoListeController todoController;
    private BuecherController BuecherController;
    private LaptopController LaptopController;
    private SchrankController SchrankController;
    private KuehlschrankController KuehlschrankController;
    private MathController MathController;
    private KochManager KochManager = new KochManager();

    //win condition
    private boolean won = false;

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
    private Button menuButton;

    //Overlayers für ui
    @FXML
    private StackPane dialogueOverlayLayer;
    @FXML
    private StackPane menuOverlayLayer;
    private at.ac.hcw.kanuescape.ui.MenuOverlayManager menuManager;
    @FXML
    private StackPane startOverlayLayer;
    private at.ac.hcw.kanuescape.ui.StartScreenOverlayManager startManager;
    @FXML
    private StackPane endOverlayLayer;
    private at.ac.hcw.kanuescape.ui.EndScreenOverlayManager endManager;

    private StackPane dialogueNode;  // Node merken, um es zu entfernen
    private boolean dialogueOpen = false;
    // dialogue ausnahme für kochmanager
    private DialogueManager dialogueManager = new DialogueManager(KochManager);

    private DialogueBoxController dialogueController;
    private Runnable afterDialogueClose = null;             // Bücherrätsel nach Dialogue close

    // Input/loop
    private final Map<KeyCode, Boolean> keys = new EnumMap(KeyCode.class);
    private AnimationTimer loop;


    // verhindert Dialogue-Spam beim Gedrückthalten
    private int exitLineIdx = 0;
    private long lastDoorDialogueNs = 0;
    private static final long DOOR_DIALOGUE_COOLDOWN_NS = 600_000_000L; // 600ms


    @FXML
    private void initialize() throws Exception {

        // StartScreen
        startManager = new at.ac.hcw.kanuescape.ui.StartScreenOverlayManager(startOverlayLayer);
        startManager.load();
        AudioManager.get().playMusicLoop(AudioPaths.MUSIC_START_END, true);
        setMenuButtonVisible(false);
        startManager.setOnStart(() -> {
            startManager.close();
            root.requestFocus();
            AudioManager.get().playMusicLoop(AudioPaths.MUSIC_GAME);
            startIntro(); // menu bleibt aus, bis Intro fertig ist
        });


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

        // DialogueBox
        loadDialogueBox();

        // Menu
        menuManager = new at.ac.hcw.kanuescape.ui.MenuOverlayManager(menuOverlayLayer);
        menuManager.load();
        menuManager.setOnNewGame(this::restartGame);
        menuManager.setOnExit(() -> Platform.exit());

        // EndScreen
        endManager = new at.ac.hcw.kanuescape.ui.EndScreenOverlayManager(endOverlayLayer);
        endManager.load();
        setMenuButtonVisible(false);
        endManager.setOnExit(() -> {
            Platform.exit();
        });


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
//        BuecherStage.initStyle(StageStyle.TRANSPARENT);
        BuecherStage.setScene(sceneBuecher);
        BuecherStage.setTitle("Bücherregal");
        // changes the icon of window
        BuecherStage.getIcons().add(
                new Image(GameController.class.getResourceAsStream("/assets/images/icon/icon.png"))
        );
        BuecherStage.setOnShown(e ->
                AudioManager.get().playMusicLoop(AudioPaths.MUSIC_RIDDLE_BOOKCASE, true)
        );


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

        // Laptop Music
        LaptopStage.setOnShown(e ->
                AudioManager.get().playMusicLoop(AudioPaths.MUSIC_RIDDLE_PROGRAMMING, true)
        );
        LaptopStage.setOnHidden(e ->
                AudioManager.get().playMusicLoop(AudioPaths.MUSIC_GAME, true)
        );


        // Closing window via ESC
        sceneLaptop.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                LaptopStage.close();
                e.consume();
            }
        });

        // changes the icon of window
        LaptopStage.getIcons().add(
                new Image(GameController.class.getResourceAsStream("/assets/images/icon/icon.png"))
        );

        // Siegbedingung checked upon closing of laptop-window
        LaptopStage.setOnHiding(e -> {
            if (LaptopController != null && LaptopController.isSolved()) {
                CheckProg(); // use to check to-do?
            }
        });

        FXMLLoader fxmlLoaderMath = new FXMLLoader(GameController.class.getResource("/fxml/Math.fxml"));
        Scene sceneMath = new Scene(fxmlLoaderMath.load(), 720, 520);
        MathController = fxmlLoaderMath.getController();

        MathStage = new Stage();
        MathStage.setTitle("Math");
        MathStage.setAlwaysOnTop(true);
        MathStage.setResizable(false);
        MathStage.initStyle(StageStyle.DECORATED);
        MathStage.setScene(sceneMath);
        MathStage.sizeToScene();

        // Math Music
        MathStage.setOnShown(e ->
                AudioManager.get().playMusicLoop(AudioPaths.MUSIC_RIDDLE_MATH, true)
        );
        MathStage.setOnHidden(e ->
                AudioManager.get().playMusicLoop(AudioPaths.MUSIC_GAME, true)
        );


        // changes the icon of window
        MathStage.getIcons().add(
                new Image(GameController.class.getResourceAsStream("/assets/images/icon/icon.png"))
        );

        // Closing window via ESC
        sceneMath.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                MathStage.close();
                e.consume();
            }
        });

        MathController.setOnSolved(() -> {
            CheckMathe();
            MathStage.close();
        });

        // ! Adaptions until here!

        // Resets the visual effects on the main root node when the book stage is closed
        BuecherStage.setOnHiding(event -> {
            // Remove the blur or any other effect from the background immediately upon hiding
            root.setEffect(null);

            AudioManager.get().playMusicLoop(AudioPaths.MUSIC_GAME, true);

            if (BuecherController != null && BuecherController.isSolved()) {
                CheckBuecher(); // use to check to-do?
            }
        });
    }

    // Scene based init: key handling + game loop
    public void init(Scene scene) {
        scene.setOnKeyPressed(e -> keys.put(e.getCode(), true));
        scene.setOnKeyReleased(e -> keys.put(e.getCode(), false));

        root.setOnMouseClicked(e -> root.requestFocus());
        Platform.runLater(() -> root.requestFocus());

        //Button click SFX
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED,e ->
        {
            Node n = (Node) e.getTarget();
            while (n != null && !(n instanceof Button)) {
                n = n.getParent();
            }
            if (n instanceof Button) {
                AudioManager.get().playSfx(AudioPaths.SFX_BTN_CLICK);
            }
        });
        //SFX hover
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            Node n = (Node) e.getTarget();

            // hochlaufen bis Button
            while (n != null && !(n instanceof Button)) {
                n = n.getParent();
            }

            if (n instanceof Button) {
                long now = System.nanoTime();
                if (now - lastHoverSfxNs > HOVER_COOLDOWN_NS) {
                    AudioManager.get().playSfx(AudioPaths.SFX_BTN_HOVER);
                    lastHoverSfxNs = now;
                }
            }
        });


        // Game loop
        loop = new AnimationTimer() {
            long last = 0;


            @Override
            public void handle(long now) {

                if (startManager != null && startManager.isOpen()) {
                    return;
                }

                //no mvmt when text box open
                if (dialogueOpen
                        || (menuManager != null && menuManager.isPaused())
                        || (endManager != null && endManager.isOpen())) {
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
        gc.setImageSmoothing(false);

        double w = gameCanvas.getWidth();
        double h = gameCanvas.getHeight();

        gc.setFill(BACKGROUND);
        gc.fillRect(0, 0, w, h);

        if (map == null || tsx == null || tilesetImage == null) {
            return;
        }

        int firstGid = map.tilesets().get(0).firstgid();
        int columns = tsx.columns();

        renderLayerByName(gc, "floor", firstGid, columns);
        renderLayerByName(gc, "floor_help", firstGid, columns);
        renderLayerByName(gc, "objects_back", firstGid, columns);
        renderLayerByName(gc, "objects", firstGid, columns);
        renderLayerByName(gc, "objects_front", firstGid, columns);
        renderLayerByName(gc, "collision", firstGid, columns);

        renderPlayer(gc);
    }

    /**
     * Findet einen Layer anhand des Namens und rendert ihn, wenn es ein TileLayer ist.
     */
    private void renderLayerByName(GraphicsContext gc, String layerName, int firstGid, int columns) {
        for (var layer : map.layers()) {
            if (layer.isTileLayer() && layerName.equals(layer.name())) {
                rc = renderer.renderTileLayer(gc, map, layer, tilesetImage, firstGid, columns);

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
                    collisionLayer = layer;
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

        double scale = Math.min(canvasW / mapW, canvasH / mapH);

        double renderW = mapW * scale;
        double renderH = mapH * scale;

        double offsetX = (canvasW - renderW) / 2.0;
        double offsetY = (canvasH - renderH) / 2.0;

        int baseX = (int) Math.round(offsetX);
        int baseY = (int) Math.round(offsetY);
        int scaledTileW = (int) Math.round(tileW * scale);
        int scaledTileH = (int) Math.round(tileH * scale);

        double frameW = playerSprite.getWidth() / Player.SPRITE_COLS;
        double frameH = playerSprite.getHeight() / Player.SPRITE_ROWS;
        double sx = player.getFrameCol() * frameW;
        double sy = player.getFrameRow() * frameH;

        double aspect = frameW / frameH;
        int targetH = (int) Math.round(scaledTileH * 1.2);
        int targetW = (int) Math.round(targetH * aspect);

        double tileX = player.getTileX();
        double tileY = player.getTileY();
        int tilePxX = (int) Math.round(tileX * scaledTileW);
        int tilePxY = (int) Math.round(tileY * scaledTileH);

        int dx = baseX + tilePxX + (scaledTileW - targetW) / 2;
        int dy = baseY + tilePxY + (scaledTileH - targetH) / 2;

        dy -= (int) Math.round(scaledTileH * PLAYER_Y_ANCHOR);

        gc.drawImage(playerSprite, sx, sy, frameW, frameH, dx, dy, targetW, targetH);
    }

    public void stop() {
        if (loop != null) {
            loop.stop();
        }
    }

    private void handleMapClick(double mouseX, double mouseY) {
        // kein mapclick when intro oder dialogue
        if (introRunning) return;
        if (endRunning) return;
        if (dialogueOpen) return;
        if (renderContext == null || interactionLayer == null) return;

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

        //crash fest for restart
        int gid2 = 0;
        if (interactionLayer2 != null && interactionLayer2.data() != null) {
            gid2 = interactionLayer2.data()[index];
        }


        int clickedGid = (gid2 != 0) ? gid2 : gid;

        onTileClicked(tileX, tileY, clickedGid);
    }

    private void onTileClicked(double x, double y, int gid) {
        if (gid == 0) return;

        double tileX = player.getGridX();
        double tileY = player.getGridY();

//        System.out.println("Tile geklickt: (" + x * 32 + "," + y * 32 + ") GID=" + gid);
//        System.out.println("Tile geklickt: (" + x + "," + y);
//        System.out.println("Player geklickt: (" + tileX + "," + tileY);

        if (Math.abs(tileX - x) <= 2 && Math.abs(tileY - y) <= 2) {

            if (gid == 60) {
                if (todoStage != null) {
                    todoStage.setX((rc.gc().getCanvas().getWidth()) / 2);
                    todoStage.setY(50);
                    todoStage.show();
                }
            }

            if (gid == 55 || gid == 56) {
                if (SchrankStage != null) {
                    SchrankStage.setX((rc.gc().getCanvas().getWidth()) / 2);
                    SchrankStage.setY(50);
                    SchrankStage.show();
                }
            }

            if (gid == 72) {

                // music
                if (!Kochen) AudioManager.get().playMusicLoop(AudioPaths.MUSIC_RIDDLE_COOKING, true);

                if (KuehlschrankStage != null) {
                    KuehlschrankStage.setX((rc.gc().getCanvas().getWidth()) / 2);
                    KuehlschrankStage.setY(50);
                    KuehlschrankStage.show();
                }
            }

            if (gid == 94 || gid == 93 || gid == 82 || gid == 81) {
                String bookcaseText = DialogueTexts.VARIANTS.get("bookcase").get(0);

                openDialogue(bookcaseText, "bookcase", () -> {
                    if (BuecherStage != null) {
                        root.setEffect(new GaussianBlur(20));

                        BuecherStage.show();
                        BuecherStage.setX(470);
                        BuecherStage.setY(210);

                        Parent content = BuecherStage.getScene().getRoot();

                        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), content);
                        fadeIn.setFromValue(0.0);
                        fadeIn.setToValue(1.0);
                        fadeIn.play();

                        if (BuecherController != null) {
                            BuecherController.getBuecherScene().requestFocus();
                        }
                    }
                });
                return;
            }

            if (gid == 90) {
                if (LaptopStage != null) {
                    LaptopStage.setX(rc.renderW() / 2);
                    LaptopStage.show();

                }
            }

            if (gid == 77 || gid == 78) {
                if (MathStage != null) {
                    MathStage.setX(rc.renderW() / 2);
                    MathStage.show();
                }
            }

            String type = dialogueManager.typeForGid(gid);
            if (type != null) {

                if ("picture".equals(type)) {
                    dialogueManager.resetPicture();
                    openDialogue(dialogueManager.nextPictureLine(), "picture");
                } else {
                    openDialogue(dialogueManager.nextTextForType(type), type);
                }
                if (gid == 63) {
                    if (KochManager.getState() == KochManager.getFINISHED()) {
                        CheckKochen();
                    }
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
            AudioManager.get().playSfx(AudioPaths.SFX_RIDDLE_DONE);     //SFX
        }
    }

    @FXML
    public void CheckKochen() {
        if (todoController != null) {
            todoController.CheckKochen(true);
            Kochen = true;

            AudioManager.get().playMusicLoop(AudioPaths.MUSIC_GAME, true);  //zurück zu game musik
            AudioManager.get().playSfx(AudioPaths.SFX_RIDDLE_DONE);     //SFX

        }
    }

    @FXML
    public void CheckMathe() {
        if (todoController != null) {
            todoController.CheckMathe(true);
            Mathe = true;
            AudioManager.get().playSfx(AudioPaths.SFX_RIDDLE_DONE);     //SFX
        }
    }

    @FXML
    public void CheckProg() {
        if (todoController != null) {
            todoController.CheckProg(true);
            Prog = true;
            AudioManager.get().playSfx(AudioPaths.SFX_RIDDLE_DONE);     //SFX
        }
    }

    public void Win() {
        if (!(Prog && Mathe && Kochen && Buecher)) return;
        if (won) return;           // verhindert mehrfach-trigger
        if (endRunning) return;    // safety

        won = true;

        AudioManager.get().playSfx(AudioPaths.SFX_ALL_DONE); // <-- Erfolgssound

        // während End-Text soll nichts mehr gehen
        introRunning = false;      // nur falls du irgendwo intro noch true hättest
        setMenuButtonVisible(false);

        // End-Text starten (Endscreen kommt erst NACH Text)
        startEnd();

        // optional: loop stoppen, damit absolut nix mehr passiert im Hintergrund
        if (loop != null) loop.stop();

    }

    private boolean isTileBlocked(int nextGridX, int nextGridY, boolean up, boolean down, boolean left, boolean right) {
        if (collisionLayer == null || renderContext == null) return false;

        int targetX = nextGridX;
        int targetY = nextGridY;

        if (nextGridX < 0 || nextGridY < 0 ||
                nextGridX >= collisionLayer.width() || nextGridY >= collisionLayer.height()) {
            return true;
        }
        int index;
        if (up) {
            index = (nextGridY - 1) * 20 + nextGridX;
        } else if (down) {
            index = (nextGridY + 1) * 20 + nextGridX;
        } else if (left) {
            index = nextGridY * 20 + nextGridX - 1;
        } else if (right) {
            index = nextGridY * 20 + nextGridX + 1;
        } else {
            return false;
        }

        if (targetX < 0 || targetY < 0 || targetX >= 20 || targetY >= collisionLayer.height()) {
            return true;
        }

//        System.out.println(index);
        int gid = collisionLayer.data()[index];
//        System.out.println(player.getGridX() + " " + player.getGridY());

        // EXIT: text bis win, dann Win()
        if (gid == 92) {
            if (Prog && Mathe && Kochen && Buecher) {
                Win();
            } else {
                showDoorDialogue("exit");
            }
            return true;
        }

//        if (gid == 92) {
//            Win();
//        }
       //SFX Wall bump
        boolean blocked = gid != 0;

        if (blocked) {
            long now = System.nanoTime();
            if (now - lastBumpSfxNs > BUMP_COOLDOWN_NS) {
                AudioManager.get().playSfx(AudioPaths.SFX_WALL_BUMP);
                lastBumpSfxNs = now;
            }
        }

        return blocked;
    }

    private void loadDialogueBox() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ui/DialogueBox.fxml"));
            dialogueNode = loader.load();
            dialogueController = loader.getController();

            // nur einmal einhängen
            dialogueOverlayLayer.getChildren().add(dialogueNode);

            // ABER: nicht anzeigen beim Start
            dialogueOverlayLayer.setVisible(false);
            dialogueOverlayLayer.setManaged(false);
            dialogueOpen = false;

            // Klick schließt (Handler ist ok, wirkt nur wenn sichtbar)
            dialogueOverlayLayer.setOnMouseClicked(e -> {

                if (dialogueController.onUserClick()) {
                    e.consume();
                    return;
                }

                // Intro Text
                if ("intro".equals(activeDialogueType)) {
                    introIndex++;

                    if (introIndex < DialogueTexts.INTRO_LINES.size()) {
                        // nächste Zeile anzeigen
                        openDialogue(DialogueTexts.INTRO_LINES.get(introIndex), "intro");
                    } else {
                        // Intro ist wirklich komplett fertig -> jetzt darf es schließen
                        introRunning = false;
                        closeDialogue();
                        setMenuButtonVisible(true);
                        root.requestFocus();
                    }

                    e.consume();
                    return;
                }
                // End Text
                if ("end".equals(activeDialogueType)) {
                    endIndex++;

                    if (endIndex < DialogueTexts.END_LINES.size()) {
                        openDialogue(DialogueTexts.END_LINES.get(endIndex), "end");
                    } else {
                        endRunning = false;
                        closeDialogue();

                        AudioManager.get().playMusicLoop(AudioPaths.MUSIC_START_END, true);
                        // jetzt erst Endscreen zeigen
                        endManager.open();

                        // (optional) loop stoppen - du machst das eh in Win(), dann dort rausnehmen
                        // if (loop != null) loop.stop();
                    }

                    e.consume();
                    return;
                }

                // Special Case Picture
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

                // 4) Standard
                closeDialogue();
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Intro und Ende Texte
    private void startIntro() {
        introRunning = true;
        introIndex = 0;

        // optional: Menu-Button verstecken, solange Intro läuft
        setMenuButtonVisible(false);

        activeDialogueType = "intro";
        openDialogue(DialogueTexts.INTRO_LINES.get(introIndex), "intro");
    }

    private void startEnd() {
        endRunning = true;
        endIndex = 0;

        // während End-Text: Menu aus
        setMenuButtonVisible(false);

        openDialogue(DialogueTexts.END_LINES.get(endIndex), "end");
    }

    public void restartGame() {
        // Overlays schließen
        if (menuManager != null && menuManager.isOpen()) menuManager.close();
        if (endManager != null && endManager.isOpen()) endManager.close(); // falls es close() gibt

        afterDialogueClose = null;
        activeDialogueType = null; // optional, falls du es nicht eh in closeDialogue nullst

        closeDialogue(); // stoppt typing + hides overlay

        // Alle Puzzle-Fenster zu (sonst bleiben die offen)
        if (todoStage != null) todoStage.hide();
        if (BuecherStage != null) BuecherStage.hide();
        if (LaptopStage != null) LaptopStage.hide();
        if (SchrankStage != null) SchrankStage.hide();
        if (KuehlschrankStage != null) KuehlschrankStage.hide();
        if (MathStage != null) MathStage.hide();

        // State reset
        won = false;
        endRunning = false;
        introRunning = false;
        endIndex = 0;
        introIndex = 0;
        exitLineIdx = 0;

        Prog = Mathe = Kochen = Buecher = false;

        // to do reset
        if (todoController != null) {
            todoController.resetChecks();
        }

        // Puzzle Controller reset
        if (MathController != null) {
            MathController.resetQuiz();
        }
        if (LaptopController != null) {
            LaptopController.resetPuzzle();
        }
        if (BuecherController != null) {
            BuecherController.resetPuzzle();
        }

        // KochManager reset
        if (KochManager != null) {
            KochManager.reset();
        }

        // Player zurücksetzen
        if (player != null) {
            player.resetTo(5, 4);

        }

        // Loop wieder starten (falls du ihn in Win() stoppst)
        if (loop != null) loop.start();

        // 7) UI/Fokus + neu rendern + Intro starten
        setMenuButtonVisible(true);
        Platform.runLater(() -> {
            render();
            AudioManager.get().playMusicLoop(AudioPaths.MUSIC_GAME, true);
            startIntro();
            root.requestFocus();
        });
    }

    private String activeDialogueType = null;

    public void openDialogue(String text) {
        openDialogue(text, null);
    }

    public void openDialogue(String text, String type) {
        if (dialogueController == null) return;

        activeDialogueType = type;
        dialogueController.setText(text);

        dialogueOverlayLayer.setVisible(true);
        dialogueOverlayLayer.setManaged(true);
        dialogueOpen = true;

    }

    public void openDialogue(String text, String type, Runnable onClose) {
        afterDialogueClose = onClose;
        openDialogue(text, type);
    }

    public void closeDialogue() {
        if (dialogueController != null) dialogueController.stopTyping();

        dialogueOverlayLayer.setVisible(false);
        dialogueOverlayLayer.setManaged(false);

        dialogueOpen = false;
        activeDialogueType = null;

        if (afterDialogueClose != null) {
            Runnable r = afterDialogueClose;
            afterDialogueClose = null;
            r.run();
        }
    }

    @FXML
    private void onMenuClicked() {
        if (menuManager != null) menuManager.toggle();
    }

    private void setMenuButtonVisible(boolean visible) {
        if (menuButton == null) return;
        menuButton.setVisible(visible);
        menuButton.setManaged(visible);
    }

    private void showDoorDialogue(String key) {
        long now = System.nanoTime();
        if (now - lastDoorDialogueNs < DOOR_DIALOGUE_COOLDOWN_NS) return;
        if (dialogueOpen || introRunning || endRunning) return;

        var list = DialogueTexts.VARIANTS.get(key);
        if (list == null || list.isEmpty()) return;

        if ("exit".equals(key)) {
            // nur 1-3 loopen
            int n = Math.min(3, list.size());
            String line = list.get(exitLineIdx % n);
            exitLineIdx = (exitLineIdx + 1) % n;

            openDialogue(line, key);
        } else {
            openDialogue(list.get(0), key);
        }

        lastDoorDialogueNs = now;
    }
}