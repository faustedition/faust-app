package de.faustedition.facsimile;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Finder;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
public class FacsimileFinder extends Finder implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(FacsimileFinder.class);
    public static final int TILE_SIZE = 256;

    @Autowired
    private Environment environment;
    private File home;
    private String fileExtension;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.home = environment.getRequiredProperty("facsimile.home", File.class);
        this.fileExtension = environment.getRequiredProperty("facsimile.extension", String.class);
        Assert.isTrue(home.isDirectory(), home + " is not a directory");
    }

    @Override
    public ServerResource find(Request request, Response response) {
        try {
            final File facsimile = findFacsimile(request.getResourceRef().getRelativeRef().getPath());
            final Form query = request.getResourceRef().getQueryAsForm();
            if (query.getFirst("metadata") != null) {
                return new FacsimileMetadataResource(facsimile);
            }

            final int x = Integer.parseInt(query.getFirstValue("x", "0"));
            final int y = Integer.parseInt(query.getFirstValue("y", "0"));
            final int zoom = Integer.parseInt(query.getFirstValue("zoom", "1"));
            return new FacsimileResource(facsimile, x, y, zoom);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public File findFacsimile(String path) throws IOException, IllegalArgumentException {
        final File facsimile = new File(home, path + "." + fileExtension);
        Preconditions.checkArgument(facsimile.isFile() && isInHome(facsimile), path);
        return facsimile;
    }

    protected static ImageReader getImageReader(File file) throws IOException {
        final ImageInputStream imageInputStream = ImageIO.createImageInputStream(file);

        final Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
        Preconditions.checkArgument(readers.hasNext(), file + " cannot be read via Java ImageIO");

        final ImageReader imageReader = readers.next();
        imageReader.setInput(imageInputStream);
        return imageReader;
    }

    public static class FacsimileMetadataResource extends ServerResource {
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        private final File facsimile;

        public FacsimileMetadataResource(File facsimile) {
            this.facsimile = facsimile;
        }

        @Get("json")
        public Representation metadata() throws IOException {
            ImageReader reader = null;
            try {
                reader = getImageReader(facsimile);
                final Map<String, Object> metadata = Maps.newHashMap();
                metadata.put("width", reader.getWidth(0));
                metadata.put("height", reader.getHeight(0));
                metadata.put("tileSize", TILE_SIZE);
                return new StringRepresentation(OBJECT_MAPPER.writeValueAsString(metadata), MediaType.APPLICATION_JSON);

            } finally {
                if (reader != null) {
                    reader.dispose();
                }
            }
        }
    }

    public static class FacsimileResource extends ServerResource {

        private final File facsimile;
        private final int zoom;
        private final int x;
        private final int y;

        public FacsimileResource(File facsimile, int x, int y, int zoom) {
            super();
            this.facsimile = facsimile;
            this.x = x;
            this.y = y;
            this.zoom = zoom;
        }

        @Get("jpg")
        public Representation image() {
            return new OutputRepresentation(MediaType.IMAGE_JPEG) {
                @Override
                public void write(OutputStream outputStream) throws IOException {
                    ImageReader reader = null;
                    ImageWriter writer = null;
                    try {
                        reader = getImageReader(facsimile);
                        final int imageWidth = reader.getWidth(0);
                        final int imageHeight = reader.getHeight(0);
                        final int tileSize = TILE_SIZE * zoom;
                        final int tileX = x * tileSize;
                        final int tileY = y * tileSize;
                        final ImageReadParam parameters = reader.getDefaultReadParam();
                        final Rectangle tile = new Rectangle(
                                Math.min(tileX, imageWidth),
                                Math.min(tileY, imageHeight),
                                Math.min(tileSize, imageWidth - tileX),
                                Math.min(tileSize, imageHeight - tileY)
                        );
                        parameters.setSourceRegion(tile);
                        parameters.setSourceSubsampling(zoom, zoom, 0, 0);

                        LOG.debug("Writing {} of {} (zoom {})", new Object[]{tile, facsimile, zoom});
                        final Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("JPEG");
                        Preconditions.checkState(writers.hasNext());
                        writer = writers.next();
                        writer.setOutput(ImageIO.createImageOutputStream(outputStream));
                        writer.write(reader.read(0, parameters));
                    } finally {
                        if (writer != null) {
                            writer.dispose();
                        }
                        if (reader != null) {
                            reader.dispose();
                        }
                    }
                }
            };
        }
    }

    private boolean isInHome(File file) {
        File parent = file.getParentFile();
        while (parent != null) {
            if (parent.equals(home)) {
                return true;
            }
            parent = parent.getParentFile();
        }
        return false;
    }

}
