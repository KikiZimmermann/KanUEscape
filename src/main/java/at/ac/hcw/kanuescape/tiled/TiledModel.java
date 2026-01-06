package at.ac.hcw.kanuescape.tiled;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * TiledModel
 *
 * Reine Datenmodelle für Tiled (JSON + TSX).
 *
 * - Keine Lade-Logik (das macht MapLoader)
 * - Keine Render-Logik (das macht MapRenderer)
 *
 * Jackson befüllt diese Records direkt aus dem JSON.
 */

public class TiledModel {

    /**
     * Root-Objekt des Tiled-JSON (Map).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TiledMap(
            int width,
            int height,
            int tilewidth,
            int tileheight,
            List<TiledLayer> layers,
            List<TiledTilesetRef> tilesets
    ) { }

    /**
     * Layer aus Tiled.
     * - type = "tilelayer"  -> data[] ist befüllt
     * - type = "objectgroup"-> data ist normalerweise null (Objects wären extra Modell)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TiledLayer(
            String name,
            String type,
            int width,
            int height,
            int[] data
    ) {
        public boolean isTileLayer() {
            return "tilelayer".equals(type);
        }

        public boolean isObjectGroup() {
            return "objectgroup".equals(type);
        }
    }

    /**
     * Tileset-Referenz aus dem JSON:
     * firstgid + source (tsx-Datei).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TiledTilesetRef(
            int firstgid,
            String source
    ) { }

    /**
     * Parsed aus TSX:
     * Tile-Größe, Spaltenanzahl, Bildinfos.
     */
    public record TsxTileset(
            int tilewidth,
            int tileheight,
            int columns,
            String imageSource,
            int imageWidth,
            int imageHeight
    ) { }
}
