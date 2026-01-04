package at.ac.hcw.kanuescape.gamescene;

import java.util.List;

import at.ac.hcw.kanuescape.gamescene.map.MapLoader;
import at.ac.hcw.kanuescape.gamescene.map.TileLayer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class MapRenderer {
    private static final int TILE_SIZE = 32;
    private final Image tileset;
    private final Image nerdyGuySheet;

    public MapRenderer() {
        this.tileset = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/assets/tiles/tileset.png"),
                "tileset.png not found"
        ));

        this.nerdyGuySheet = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/assets/sprite/nerdyguy_sprite.png"),
                "nerdyguy_sprite.png not found"
        ));
    }

    public void renderTestGrid(GraphicsContext gc) {
        // Test: 10x8 Tiles aus dem Tileset (oben links)
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 10; x++) {
                gc.drawImage(
                        tileset,
                        x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE,
                        x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE
                );
            }
        }
        gc.strokeRect(0, 0, 10 * TILE_SIZE, 8 * TILE_SIZE);
        gc.fillText("TEST GRID 10x8", 5, 12);

    }

    public String loadMapJson(String resourcePath) {
        try (var in = Objects.requireNonNull(
                getClass().getResourceAsStream(resourcePath),
                "Map not found on classpath: " + resourcePath
        )) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read map: " + resourcePath, e);
        }
    }

    public void renderMap(GraphicsContext gc, String mapPath, List<String> layerOrder,
                          double canvasW, double canvasH) {
        try {
            // Wir nehmen floor als Referenz für Offset (Map-Größe)
            TileLayer ref = MapLoader.loadTileLayer(mapPath, "floor");

            double mapPixelW = ref.width * TILE_SIZE;
            double mapPixelH = ref.height * TILE_SIZE;
            double offsetX = (canvasW - mapPixelW) / 2.0;
            double offsetY = (canvasH - mapPixelH) / 2.0;

            for (String layerName : layerOrder) {
                TileLayer layer = MapLoader.loadTileLayerOrNull(mapPath, layerName);
                if (layer == null) continue;

                renderLayerWithOffset(gc, layer, offsetX, offsetY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderLayerWithOffset(GraphicsContext gc, TileLayer layer, double offsetX, double offsetY) {
        int tilesetCols = (int) (tileset.getWidth() / TILE_SIZE);

        for (int y = 0; y < layer.height; y++) {
            for (int x = 0; x < layer.width; x++) {
                int gid = layer.getTile(x, y);
                if (gid == 0) continue;

                int local = gid - layer.firstGid;
                if (local < 0) continue;

                int sx = (local % tilesetCols) * TILE_SIZE;
                int sy = (local / tilesetCols) * TILE_SIZE;

                gc.drawImage(
                        tileset,
                        sx, sy, TILE_SIZE, TILE_SIZE,
                        offsetX + x * TILE_SIZE, offsetY + y * TILE_SIZE, TILE_SIZE, TILE_SIZE
                );
            }
        }
    }

    public void renderNerdyGuy(GraphicsContext gc, double canvasW, double canvasH, TileLayer refLayer,
                               int tileX, int tileY, int col, int row) {

        // Map zentriert wie bei renderMap()
        double mapPixelW = refLayer.width * TILE_SIZE;
        double mapPixelH = refLayer.height * TILE_SIZE;
        double offsetX = (canvasW - mapPixelW) / 2.0;
        double offsetY = (canvasH - mapPixelH) / 2.0;

        // Frame-Boxen (aus PNG)
        int[] xs = {0, 37, 73};
        int[] ys = {0, 49, 97, 146};
        int fw = 28;
        int fh = 37;

        int sx = xs[Math.max(0, Math.min(col, xs.length - 1))];
        int sy = ys[Math.max(0, Math.min(row, ys.length - 1))];


        double tilePx = offsetX + tileX * TILE_SIZE;
        double tilePy = offsetY + tileY * TILE_SIZE;

        // bottom-center: Sprite mittig auf Tile, Füße auf Unterkante
        double dx = tilePx + (TILE_SIZE - fw) / 2.0;
        double dy = tilePy + (TILE_SIZE - fh);


        // optional: damit "Füße" besser auf der Tile stehen (kleiner Down-Offset)
        dy += (TILE_SIZE - fh);

        gc.drawImage(
                nerdyGuySheet,
                sx, sy, fw, fh,      // source
                dx, dy, fw, fh       // destination (1:1)
        );
    }


}
