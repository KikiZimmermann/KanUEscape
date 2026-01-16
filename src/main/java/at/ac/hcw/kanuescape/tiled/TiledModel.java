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
            int[] data,
            java.util.List<TiledObject> objects
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TiledObject(
            int id,
            String name,
            String type,
            double x,
            double y,
            double width,
            double height,
            java.util.List<TiledProperty> properties
    ) {
        public String propString(String key) {
            if (properties == null) return null;
            for (var p : properties) if (key.equals(p.name())) return String.valueOf(p.value());
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TiledProperty(
            String name,
            String type,
            Object value
    ) { }
}
