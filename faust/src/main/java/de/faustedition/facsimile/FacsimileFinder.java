package de.faustedition.facsimile;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import eu.interedition.image.ImageFile;
import eu.interedition.image.transform.Crop;
import eu.interedition.image.transform.Rotate;
import eu.interedition.image.transform.Scale;
import eu.interedition.image.transform.TransformList;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
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

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
public class FacsimileFinder extends Finder implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(FacsimileFinder.class);

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
            final float zoom = Float.parseFloat(query.getFirstValue("zoom", "1"));
            final int rotate = Integer.parseInt(query.getFirstValue("rotate", "0"));
            final Dimension facsimileSize = facsimile.getSize().getSize();
            final float tileSize = 256 / zoom;
            return new FacsimileResource(facsimile,
                    1 / zoom,
                    (((float) facsimileSize.getWidth()) / zoom - tileSize) / 2.0f,
                    (((float) facsimileSize.getHeight()) / zoom - tileSize) / 2.0f,
                    tileSize, tileSize, rotate);
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

    public static class FacsimileResource extends ServerResource {

        private final ImageFile facsimile;
        private final float scale;
        private final float x;
        private final float y;
        private final float width;
        private final float height;
        private final int rotate;

        public FacsimileResource(ImageFile facsimile, float scale, float x, float y, float width, float height, int rotate) {
            this.facsimile = facsimile;
            this.scale = scale;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.rotate = rotate;
        }

        @Get("jpg")
        public Representation path() throws IOException {
            return new OutputRepresentation(MediaType.IMAGE_JPEG) {
                @Override
                public void write(OutputStream outputStream) throws IOException {
                    LOG.debug("Writing {}x{}+{}+{} of {} (scale {}, rotate {})", new Object[] { width, height, x, y, facsimile, scale, rotate});
                    ImageFile.write(new TransformList(Lists.newArrayList(new Scale(2, scale), new Crop(x, y, width, height), new Rotate(2, rotate))).apply(facsimile.read()), "JPEG", outputStream);
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
