package de.faustedition.facsimile;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface Facsimiles {

    int TILE_SIZE = 256;
    String IMAGE_FILE_EXTENSION = ".tif";

    FacsimileMetadata metadata(String path) throws IOException, IllegalArgumentException;

    BufferedImage tile(String path, int zoom, int x, int y) throws IOException, IllegalArgumentException;

}
