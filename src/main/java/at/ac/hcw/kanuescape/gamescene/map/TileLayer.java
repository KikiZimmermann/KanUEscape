package at.ac.hcw.kanuescape.gamescene.map;

public class TileLayer {
    public final int width;
    public final int height;
    public final int[] data;
    public final int firstGid;

    public TileLayer(int width, int height, int[] data, int firstGid) {
        this.width = width;
        this.height = height;
        this.data = data;
        this.firstGid = firstGid;
    }

    public int getTile(int x, int y) {
        return data[y * width + x];
    }
}
