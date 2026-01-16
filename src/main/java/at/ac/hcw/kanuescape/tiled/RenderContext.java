package at.ac.hcw.kanuescape.tiled;

public record RenderContext(double scale,
                            int baseX,
                            int baseY,
                            int tileW,
                            int tileH,
                            double renderW,
                            double renderH
) {

    @Override
    public double renderW() {
        return renderW;
    }
}

