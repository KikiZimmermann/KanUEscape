package at.ac.hcw.kanuescape.tiled;

import javafx.scene.canvas.GraphicsContext;

public record RenderContext(double scale, int baseX, int baseY, int tileW, int tileH , double renderW, double renderH, GraphicsContext gc) {

    @Override
    public double renderW() {
        return renderW;
    }
    public double renderH() {
        return renderH;
    }
    public GraphicsContext gc() {
        return gc;
    }
}

