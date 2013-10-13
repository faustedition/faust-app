package de.faustedition.facsimile;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class DefaultFacsimiles implements Facsimiles {

    private final File facsimileDirectory;

    public DefaultFacsimiles(File facsimileDirectory) {
        this.facsimileDirectory = facsimileDirectory;
    }

    public FacsimileMetadata metadata(String path) throws IOException, IllegalArgumentException {
        ImageReader reader = null;
        try {
            reader = reader(path);
            return new FacsimileMetadata(reader.getWidth(0), reader.getHeight(0), reader.getNumImages(true) - 1, TILE_SIZE);
        } finally {
            if (reader != null) {
                reader.dispose();
            }
        }
    }

    public BufferedImage tile(final String path, int zoom, int x, int y) throws IOException, IllegalArgumentException {
        ImageReader reader = null;
        try {
            x = (Math.max(0, x) * Facsimiles.TILE_SIZE);
            y = (Math.max(0, y) * Facsimiles.TILE_SIZE);
            reader = reader(path);
            final int index = Math.max(0, Math.min(zoom, reader.getNumImages(true) - 1));
            final ImageReadParam parameters = reader.getDefaultReadParam();
            parameters.setSourceRegion(clip(x, y, Facsimiles.TILE_SIZE, Facsimiles.TILE_SIZE, reader.getWidth(index), reader.getHeight(index)));
            return reader.read(index, parameters);
        } finally {
            if (reader != null) {
                reader.dispose();
            }
        }
    }
    public ImageReader reader(String path) throws IOException, IllegalArgumentException {
        final File facsimile = new File(facsimileDirectory, path + IMAGE_FILE_EXTENSION);
        Preconditions.checkArgument(facsimile.isFile() && isInHome(facsimile), path);
        final ImageInputStream imageInputStream = ImageIO.createImageInputStream(facsimile);
        final ImageReader imageReader = Iterators.get(ImageIO.getImageReaders(imageInputStream), 0);
        imageReader.setInput(imageInputStream);
        return imageReader;
    }

    protected boolean isInHome(File file) {
        for (File parent = file; parent != null; parent = parent.getParentFile()) {
            if (parent.equals(facsimileDirectory)) {
                return true;
            }
        }
        return false;
    }

    protected static Rectangle clip(int x, int y, int width, int height, int maxWidth, int maxHeight) {
        x = Math.min(Math.max(0, x), maxWidth - 1);
        y = Math.min(Math.max(0, y), maxHeight - 1);
        width = Math.min(maxWidth - x, Math.max(1, width));
        height = Math.min(maxHeight - y, Math.max(1, height));
        return new Rectangle(x, y, width, height);
    }

}
