package de.faustedition.facsimile;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import eu.interedition.image.ImageFile;
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

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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
            final ImageFile facsimile = findFacsimile(request.getResourceRef().getRelativeRef().getPath());
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

    public ImageFile findFacsimile(String path) throws IOException, IllegalArgumentException {
        final File facsimile = new File(home, path + "." + fileExtension);
        if (facsimile.isFile() && isInHome(facsimile)) {
            return new ImageFile(facsimile);
        }
        throw new IllegalArgumentException(path);
    }

    public static class FacsimileMetadataResource extends ServerResource {
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        private final ImageFile facsimile;

        public FacsimileMetadataResource(ImageFile facsimile) {
            this.facsimile = facsimile;
        }

        @Get("json")
        public Representation metadata() throws IOException {
            final Rectangle size = facsimile.getSize();

            final Map<String, Object> metadata = Maps.newHashMap();
            metadata.put("width", new Double(size.getWidth()).intValue());
            metadata.put("height", new Double(size.getHeight()).intValue());
            metadata.put("tileSize", TILE_SIZE);
            return new StringRepresentation(OBJECT_MAPPER.writeValueAsString(metadata), MediaType.APPLICATION_JSON);
        }
    }

    public static class FacsimileResource extends ServerResource {

        private final ImageFile facsimile;
        private final int zoom;
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        public FacsimileResource(ImageFile facsimile, int x, int y, int zoom) {
            super();
            this.facsimile = facsimile;
            final Rectangle size = this.facsimile.getSize();
            this.x = Math.min(x * TILE_SIZE * zoom, (int) size.getWidth() * zoom);
            this.y = Math.min(y * TILE_SIZE * zoom, (int) size.getHeight() * zoom);
            this.width = Math.min(TILE_SIZE * zoom, Math.round((float) size.getWidth() * zoom - x));
            this.height = Math.min(TILE_SIZE * zoom, Math.round((float) size.getHeight() * zoom - y));
            this.zoom = zoom;
        }

        @Get("jpg")
        public Representation image() {
            return new OutputRepresentation(MediaType.IMAGE_JPEG) {
                @Override
                public void write(OutputStream outputStream) throws IOException {
                    LOG.debug("Writing [{}, {}] [{} x {}] of {} (zoom {})", new Object[]{x, y, width, height, facsimile, zoom });
                    ImageFile.write(facsimile.read(new Rectangle(x, y, width, height), zoom), "JPEG", outputStream);
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
