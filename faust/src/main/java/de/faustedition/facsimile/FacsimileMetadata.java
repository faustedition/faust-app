package de.faustedition.facsimile;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class FacsimileMetadata {

    private final int width;
    private final int height;
    private final int maxZoom;
    private final int tileSize;

    public FacsimileMetadata(int width, int height, int maxZoom, int tileSize) {
        this.width = width;
        this.height = height;
        this.maxZoom = maxZoom;
        this.tileSize = tileSize;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getMaxZoom() {
        return maxZoom;
    }

    public int getTileSize() {
        return tileSize;
    }
}
