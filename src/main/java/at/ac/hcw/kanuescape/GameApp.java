package at.ac.hcw.kanuescape;

import at.ac.hcw.kanuescape.gamescene.map.MapLoader;
import at.ac.hcw.kanuescape.gamescene.map.TileLayer;


import at.ac.hcw.kanuescape.gamescene.MapRenderer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;


public class GameApp extends Application {


    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        MapRenderer renderer = new MapRenderer();

        final int[] playerTileX = {5};
        final int[] playerTileY = {4};


        TileLayer floor;
        try {
            floor = MapLoader.loadTileLayer("/maps/game_screen.json", "floor");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        gc.setFill(javafx.scene.paint.Color.web("#2b2b2b"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        renderer.renderMap(
                gc,
                "/maps/game_screen.json",
                java.util.List.of(
                        "floor",
                        "floor_help",
                        "objects_back",
                        "objects",
                        "objects_front",
                        "interactions"
                ),
                canvas.getWidth(),
                canvas.getHeight()
        );

        renderer.renderNerdyGuy(gc, canvas.getWidth(), canvas.getHeight(), floor,
                playerTileX[0], playerTileY[0], 1, 0);


        Scene scene = new Scene(new Group(canvas));
        stage.setTitle("KanUEscape – MapRenderer Step 1");
        stage.setScene(scene);
        stage.show();

        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case W -> playerTileY[0]--;
                case S -> playerTileY[0]++;
                case A -> playerTileX[0]--;
                case D -> playerTileX[0]++;
            }
            redraw(gc, canvas, renderer, floor, playerTileX[0], playerTileY[0], 1, 0);
        });


    }

    private void redraw(GraphicsContext gc, Canvas canvas, MapRenderer renderer, TileLayer floor,
                        int playerTileX, int playerTileY, int col, int row) {
        gc.setFill(javafx.scene.paint.Color.web("#2b2b2b"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        renderer.renderMap(
                gc,
                "/maps/game_screen.json",
                java.util.List.of("floor","floor_help","objects_back","objects","objects_front","interactions"),
                canvas.getWidth(),
                canvas.getHeight()
        );

        renderer.renderNerdyGuy(gc, canvas.getWidth(), canvas.getHeight(), floor,
                playerTileX, playerTileY, col, row);
    }



    public static void main(String[] args) {
        launch(args);
    }
}
