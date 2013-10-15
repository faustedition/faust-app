package de.faustedition.resource;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Implementation of a resource collection referring to resources in the local filesystem.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class FileBasedTextResourceCollection extends TextResourceCollection {

    /**
     * The root directory of this collection.
     */
    protected final File root;

    /**
     * Registers a file-based collection with the given resolver.
     *
     * @param resolver  the resolver with which the collection will be registered
     * @param prefix    the common path prefix under which the collection's resources will be {@link TextResourceResolver#mount(String, TextResourceCollection) mounted}
     * @param root      assigned to {@link #root}
     * @param sourceURI assigned to {@link #source}
     * @param charset   assigned to {@link #charset}
     * @param maxAge    assigned to {@link #maxAge}
     * @throws IllegalArgumentException in case the provided file is not a root or cannot be read
     */
    public static void register(TextResourceResolver resolver, String prefix, File root, String sourceURI, Charset charset, long maxAge) throws IllegalArgumentException {
        Preconditions.checkArgument(root.isDirectory(), root + " is not a root");
        Preconditions.checkArgument(root.canRead(), root + " cannot be read");
        resolver.mount(prefix, new FileBasedTextResourceCollection(root, (sourceURI.replaceAll("\\/+$", "") + "/"), charset, maxAge));
    }

    /**
     * Constructor.
     *
     * @param root      assigned to {@link #root}
     * @param sourceURI assigned to {@link #source}
     * @param charset   assigned to {@link #charset}
     * @param maxAge    assigned to {@link #maxAge}
     */
    public FileBasedTextResourceCollection(File root, String sourceURI, Charset charset, long maxAge) {
        super(sourceURI, charset, maxAge);
        this.root = root;
    }

    public TextResource resolve(String relativePath) throws IOException, IllegalArgumentException {
        final File resource = new File(root, relativePath);
        Preconditions.checkArgument(resource.isFile(), resource + " is not a file");
        Preconditions.checkArgument(resource.canRead(), resource + " cannot be read");
        Preconditions.checkArgument(isDescendant(root, resource), resource + " is not contained in " + root);
        return new TextResource(Files.newInputStreamSupplier(resource), source.resolve(relativePath), charset, resource.lastModified(), maxAge);
    }

    private static boolean isDescendant(File parent, File descendant) throws IOException {
        parent = parent.getCanonicalFile();
        File toCheck = descendant.getCanonicalFile().getParentFile();
        while (toCheck != null) {
            if (toCheck.equals(parent)) {
                return true;
            }
            toCheck = toCheck.getParentFile();
        }
        return false;
    }
}
