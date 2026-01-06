package at.ac.hcw.kanuescape.tiled;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * MapRenderer
 *
 * Verantwortlich für:
 * - Zeichnen eines TileLayers aus einer Tiled-Map auf ein JavaFX-Canvas
 * - Skalierung der Map auf die Fenstergröße (fit-to-window)
 * - Zentrierung der Map
 *
 * Wichtig:
 * - Diese Klasse kennt KEINE UI-Logik
 * - Diese Klasse kennt KEINEN Player
 * - Sie rendert ausschließlich TileLayer (keine Collision, keine Interactions)
 */

public class MapRenderer {

    /**
     * Rendert einen einzelnen TileLayer.
     *
     * @param gc               GraphicsContext des Canvas
     * @param map              Geladene TiledMap (reine Daten)
     * @param layer            Der zu zeichnende TileLayer
     * @param tilesetImage     PNG des Tilesets
     * @param firstGid         firstgid aus der Map (meist 1)
     * @param tilesetColumns   Anzahl der Spalten im Tileset
     */
    public void renderTileLayer(
            GraphicsContext gc,
            TiledModel.TiledMap map,
            TiledModel.TiledLayer layer,
            Image tilesetImage,
            int firstGid,
            int tilesetColumns
    ) {
        // Sicherheitscheck: nur TileLayer mit Daten zeichnen
        if (layer.data() == null) return;


        // Tile-Größe aus der Map
        int tileW = map.tilewidth();
        int tileH = map.tileheight();

        // Canvas size
        double canvasW = gc.getCanvas().getWidth();
        double canvasH = gc.getCanvas().getHeight();

        // Map pixel size (unscaled)
        double mapW = map.width() * tileW;
        double mapH = map.height() * tileH;

        // Fit-to-window-Skalierung (Map bleibt vollständig sichtbar)
        double scale = Math.min(canvasW / mapW, canvasH / mapH);

        // Zentrierung der Map im Canvas
        double renderW = mapW * scale;
        double renderH = mapH * scale;
        double offsetX = (canvasW - renderW) / 2.0;
        double offsetY = (canvasH - renderH) / 2.0;

        // Integer-Grid (wichtig gegen Render-Seams)
        int baseX = (int) Math.round(offsetX);
        int baseY = (int) Math.round(offsetY);
        int scaledTileW = (int) Math.round(tileW * scale);
        int scaledTileH = (int) Math.round(tileH * scale);

        int[] data = layer.data();
        int layerW = layer.width();

        // Durch alle Tiles im Layer iterieren
        for (int i = 0; i < data.length; i++) {
            int gid = data[i];
            if (gid == 0) continue; // leeres Tile

            // Lokale Tile-ID im Tileset
            int localId = gid - firstGid;
            if (localId < 0) continue; // Sicherheit

            // Position im Tileset (Quelle)
            int srcCol = localId % tilesetColumns;
            int srcRow = localId / tilesetColumns;

            int sx = srcCol * tileW;
            int sy = srcRow * tileH;
            int sw = tileW;
            int sh = tileH;

            // Position im Layer (Tile-Koordinaten)
            int tileX = i % layerW;
            int tileY = i / layerW;

            // Zielposition auf dem Canvas (Pixel)
            int dx = baseX + tileX * scaledTileW;
            int dy = baseY + tileY * scaledTileH;

            // Zielgröße (skaliertes Tile)
            int dw = scaledTileW;
            int dh = scaledTileH;

            // Tile zeichnen
            gc.drawImage(tilesetImage, sx, sy, sw, sh, dx, dy, dw, dh);
        }
    }
}
