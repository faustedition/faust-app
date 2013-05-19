package de.faustedition.facsimile;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import de.faustedition.WebApplication;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Path("/facsimile")
public class FacsimileResource {

    private static final int TILE_SIZE = 256;
    private static final String IMAGE_FILE_EXTENSION = ".tif";

    private final File facsimileDir;

    private BufferedImage fallbackImage;

    @Inject
    public FacsimileResource(@Named("data.home") String dataPath) {
        this.facsimileDir = new File(dataPath, "facsimile");
        Preconditions.checkArgument(this.facsimileDir.isDirectory() || this.facsimileDir.mkdirs(), facsimileDir + " is not a directory");

        boolean facsimilesAvailable = false;
        final Queue<File> directories = new ArrayDeque<File>(Collections.singleton(facsimileDir));
        while (!directories.isEmpty()) {
            for (File file : directories.remove().listFiles()) {
                if (file.isDirectory()) {
                    directories.add(file);
                } else if (file.isFile() && file.getName().endsWith(IMAGE_FILE_EXTENSION)) {
                    facsimilesAvailable = true;
                    break;
                }
            }
            if (facsimilesAvailable) {
                break;
            }
        }
        if (!facsimilesAvailable) {
            final int width = 3 * TILE_SIZE;
            final int height = 4 * TILE_SIZE;
            this.fallbackImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            final Random random = new Random();
            for(int x = 0; x < width; x++) {
                for(int y = 0; y < height; y++) {
                    fallbackImage.setRGB(x, y, random.nextInt());
                }
            }
        }
    }


    @Path("/metadata/{path: .+?}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> metadata(@PathParam("path") final String path) throws IOException {
        final Map<String, Object> metadata = Maps.newHashMap();
        metadata.put("tileSize", TILE_SIZE);

        if (fallbackImage != null) {
            metadata.put("width", fallbackImage.getWidth());
            metadata.put("height", fallbackImage.getHeight());
            metadata.put("maxZoom", 0);
        } else {
            ImageReader reader = null;
            try {
                reader = reader(path);
                metadata.put("width", reader.getWidth(0));
                metadata.put("height", reader.getHeight(0));
                metadata.put("maxZoom", reader.getNumImages(true) - 1);
            } finally {
                if (reader != null) {
                    reader.dispose();
                }
            }
        }

        return metadata;
	}

    @Path("/{path: .+?}")
    @GET
    @Produces("image/jpeg")
    public StreamingOutput tile(@PathParam("path") final String path,
                                @QueryParam("zoom") @DefaultValue("0") int zoom,
                                @QueryParam("x") @DefaultValue("0") int x,
                                @QueryParam("y") @DefaultValue("0") int y) throws IOException {

        x = (Math.max(0, x) * TILE_SIZE);
        y = (Math.max(0, y) * TILE_SIZE);

        BufferedImage image = this.fallbackImage;
        if (image == null) {
            ImageReader reader = null;
            try {
                    reader = reader(path);
                    final int index = Math.max(0, Math.min(zoom, reader.getNumImages(true) - 1));
                    final ImageReadParam parameters = reader.getDefaultReadParam();
                    parameters.setSourceRegion(clip(x, y, TILE_SIZE, TILE_SIZE, reader.getWidth(index), reader.getHeight(index)));
                    image = reader.read(index, parameters);
            } finally {
                if (reader != null) {
                    reader.dispose();
                }
            }
        } else {
            final Rectangle rectangle = clip(x, y, TILE_SIZE, TILE_SIZE, image.getWidth(), image.getHeight());
            image = image.getSubimage(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        }

        final BufferedImage outputImage = image;
        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                ImageIO.write(outputImage, "JPEG", output);
            }
        };
    }

    @GET
    public Response index() {
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    protected Rectangle clip(int x, int y, int width, int height, int maxWidth, int maxHeight) {
        x = Math.min(Math.max(0, x), maxWidth - 1);
        y = Math.min(Math.max(0, y), maxHeight - 1);
        width = Math.min(maxWidth - x, Math.max(1, width));
        height = Math.min(maxHeight - y, Math.max(1, height));
        return new Rectangle(x, y, width, height);
    }

    protected ImageReader reader(String path) throws IOException {
        final File facsimile = new File(facsimileDir, WebApplication.path(WebApplication.pathDeque(path)) + IMAGE_FILE_EXTENSION);
        if (!facsimile.isFile() || !isInHome(facsimile)) {
            throw new WebApplicationException(Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity(path).build());
        }
        final ImageInputStream imageInputStream = ImageIO.createImageInputStream(facsimile);
        final ImageReader imageReader = Iterators.get(ImageIO.getImageReaders(imageInputStream), 0);
        imageReader.setInput(imageInputStream);
        return imageReader;
    }

    protected boolean isInHome(File file) {
		File parent = file.getParentFile();
		while (parent != null) {
			if (parent.equals(facsimileDir)) {
				return true;
			}
			parent = parent.getParentFile();
		}
		return false;
	}
}
