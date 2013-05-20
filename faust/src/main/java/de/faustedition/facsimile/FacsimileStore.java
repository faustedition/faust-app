package de.faustedition.facsimile;

import com.google.common.collect.Iterators;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class FacsimileStore {

    private static final String IMAGE_FILE_EXTENSION = ".tif";

    private final File facsimileDirectory;

    public FacsimileStore(File facsimileDirectory) {
        this.facsimileDirectory = facsimileDirectory;
    }

    public ImageReader reader(String path) throws IOException {
        final File facsimile = new File(facsimileDirectory, path + IMAGE_FILE_EXTENSION);
        if (!facsimile.isFile() || !isInHome(facsimile)) {
            throw new WebApplicationException(Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity(path).build());
        }
        final ImageInputStream imageInputStream = ImageIO.createImageInputStream(facsimile);
        final ImageReader imageReader = Iterators.get(ImageIO.getImageReaders(imageInputStream), 0);
        imageReader.setInput(imageInputStream);
        return imageReader;
    }

    public boolean isEmpty() {
        boolean empty = true;
        final Queue<File> directories = new ArrayDeque<File>(Collections.singleton(facsimileDirectory));
        while (!directories.isEmpty()) {
            for (File file : directories.remove().listFiles()) {
                if (file.isDirectory()) {
                    directories.add(file);
                } else if (file.isFile() && file.getName().endsWith(IMAGE_FILE_EXTENSION)) {
                    empty = false;
                    break;
                }
            }
            if (!empty) {
                break;
            }
        }
        return empty;
    }

    protected boolean isInHome(File file) {
        File parent = file.getParentFile();
        while (parent != null) {
            if (parent.equals(facsimileDirectory)) {
                return true;
            }
            parent = parent.getParentFile();
        }
        return false;
    }

}
