package de.faustedition.facsimile;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
public class FacsimileTileResource extends ServerResource {

    private final Ehcache cache;
    private final FacsimileTile tile;

    public FacsimileTileResource(Ehcache cache, FacsimileTile tile) {
        super();
        this.cache = cache;
        this.tile = tile;
    }

    @Get("jpg")
    public Representation image() throws IOException {
        final Element cacheElement = cache.get(tile);
        if (cacheElement != null) {
            if (cacheElement.getCreationTime() >= tile.getFile().lastModified()) {
                return new FacsimileTileRepresentation((byte[]) cacheElement.getObjectValue());
            }
            cache.remove(tile);
        }

        final ByteArrayOutputStream tileDataStream = new ByteArrayOutputStream();
        ImageReader reader = null;
        try {
            reader = tile.createImageReader();
            final int imageIndex = Math.max(0, Math.min(tile.getZoom(), reader.getNumImages(true) - 1));
            final int imageWidth = reader.getWidth(imageIndex);
            final int imageHeight = reader.getHeight(imageIndex);
            final int x = tile.getX() * FacsimileTile.SIZE;
            final int y = tile.getY() * FacsimileTile.SIZE;
            final ImageReadParam parameters = reader.getDefaultReadParam();
            final Rectangle tile = new Rectangle(
                    Math.min(x, imageWidth),
                    Math.min(y, imageHeight),
                    Math.min(FacsimileTile.SIZE, x >= imageWidth ? imageWidth : imageWidth - x),
                    Math.min(FacsimileTile.SIZE, y >= imageHeight ? imageHeight : imageHeight - y)
            );
            parameters.setSourceRegion(tile);
            final BufferedImage tileImage = reader.read(imageIndex, parameters);

            ImageIO.write(tileImage, "JPEG", tileDataStream);
        } finally {
            if (reader != null) {
                reader.dispose();
            }
        }

        final byte[] tileData = tileDataStream.toByteArray();
        cache.put(new Element(tile, tileData));
        return new FacsimileTileRepresentation(tileData);
    }

    private static class FacsimileTileRepresentation extends OutputRepresentation {

        private final byte[] data;

        public FacsimileTileRepresentation(byte[] data) {
            super(MediaType.IMAGE_JPEG);
            this.data = data;
        }

        @Override
        public void write(OutputStream outputStream) throws IOException {
            outputStream.write(data);
        }
    }
}
