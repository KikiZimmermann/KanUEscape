package at.ac.hcw.kanuescape.gamescene.map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class MapLoader {
    public static TileLayer loadTileLayer(String mapPath, String layerName) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream is = MapLoader.class.getResourceAsStream(mapPath)) {
            if (is == null) {
                throw new RuntimeException("Map not found on classpath: " + mapPath);
            }

            JsonNode root = mapper.readTree(is);
            JsonNode layers = root.get("layers");

            for (JsonNode layer : layers) {
                String type = layer.get("type").asText();
                String name = layer.get("name").asText();

                if ("tilelayer".equals(type) && layerName.equals(name)) {
                    int width = layer.get("width").asInt();
                    int height = layer.get("height").asInt();

                    JsonNode dataNode = layer.get("data");
                    int[] data = new int[width * height];
                    for (int i = 0; i < data.length; i++) {
                        data[i] = dataNode.get(i).asInt();
                    }

                    int firstGid = findFirstGidForTileset(root, "tileset.png");
                    return new TileLayer(width, height, data, firstGid);
                }
            }

            throw new RuntimeException("Tilelayer not found: " + layerName);
        }
    }

    private static int findFirstGidForTileset(JsonNode root, String tilesetFileHint) {
        JsonNode tilesets = root.get("tilesets");
        if (tilesets == null) return 1;

        for (JsonNode ts : tilesets) {
            int firstgid = ts.has("firstgid") ? ts.get("firstgid").asInt() : 1;

            // case 1: embedded tileset
            if (ts.has("image") && ts.get("image").asText().contains(tilesetFileHint)) {
                return firstgid;
            }

            // case 2: TSX referenced via "source"
            if (ts.has("source") && ts.get("source").asText().contains(".tsx")) {
                // Wir können TSX später sauber auflösen; für jetzt reicht oft:
                // wenn nur 1 tileset vorhanden ist, nehmen wir dessen firstgid.
                // Falls mehrere tilesets da sind, sag ich dir gleich wie wir TSX einlesen.
                if (tilesets.size() == 1) return firstgid;
            }
        }

        // Fallback: häufig 1
        return 1;
    }

    public static TileLayer loadTileLayerOrNull(String mapPath, String layerName) {
        try {
            return loadTileLayer(mapPath, layerName);
        } catch (Exception ex) {
            return null;
        }
    }


}
