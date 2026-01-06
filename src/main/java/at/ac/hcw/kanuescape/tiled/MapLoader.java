package at.ac.hcw.kanuescape.tiled;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.image.Image;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * MapLoader = Import-Schicht
 * - lädt Ressourcen (JSON, später TSX, PNG) aus src/main/resources (Classpath)
 * - parst JSON (Tiled Map) -> TiledModel.TiledMap
 * - parst TSX (Tileset)    -> TiledModel.TsxTileset
 * - lädt PNG               -> Image
 *
 * Keine Render-Logik, keine Game-Logik.
 */

public class MapLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Lädt und parst eine Tiled-Map (JSON) aus dem Classpath.
     */
    public static TiledModel.TiledMap loadMap(String jsonPath) {
        String json = loadResourceAsString(jsonPath);
        try {
            return MAPPER.readValue(json, TiledModel.TiledMap.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse map JSON: " + jsonPath, e);
        }
    }

    /**
     * Lädt und parst ein TSX-Tileset aus dem Classpath.
     */
    public static TiledModel.TsxTileset loadTsxTileset(String tsxPath) {
        try (InputStream in = MapLoader.class.getResourceAsStream(tsxPath)) {
            if (in == null) throw new IllegalStateException("TSX not found: " + tsxPath);

            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(in);

            Element tilesetEl = doc.getDocumentElement(); // <tileset ...>

            int tileW = parseIntAttr(tilesetEl, "tilewidth", tsxPath);
            int tileH = parseIntAttr(tilesetEl, "tileheight", tsxPath);

            int columns = 0;
            String colAttr = tilesetEl.getAttribute("columns");
            if (colAttr != null && !colAttr.isBlank()) {
                columns = Integer.parseInt(colAttr);
            }

            Element imageEl = (Element) tilesetEl.getElementsByTagName("image").item(0);
            if (imageEl == null) {
                throw new IllegalStateException("TSX has no <image> tag: " + tsxPath);
            }

            String imageSource = imageEl.getAttribute("source");
            int imageW = parseIntAttr(imageEl, "width", tsxPath);
            int imageH = parseIntAttr(imageEl, "height", tsxPath);

            return new TiledModel.TsxTileset(tileW, tileH, columns, imageSource, imageW, imageH);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse TSX: " + tsxPath, e);
        }
    }

    /**
     * Lädt ein Bild aus dem Classpath.
     */
    public static Image loadImage(String imagePath) {
        try (InputStream in = MapLoader.class.getResourceAsStream(imagePath)) {
            if (in == null) throw new IllegalStateException("Image not found: " + imagePath);
            return new Image(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load image: " + imagePath, e);
        }
    }

    /**
     * Hilfsfunktion: Ressource als String laden (für JSON / TSX).
     */
    public static String loadResourceAsString(String path) {
        try (InputStream in = MapLoader.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("Resource not found: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read resource: " + path, e);
        }
    }

    /**
     * Kleine Helper, damit Fehler beim TSX-Parsing verständlicher werden.
     */
    private static int parseIntAttr(Element el, String attrName, String tsxPath) {
        String value = el.getAttribute(attrName);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing attribute '" + attrName + "' in TSX: " + tsxPath);
        }
        return Integer.parseInt(value);
    }
}
