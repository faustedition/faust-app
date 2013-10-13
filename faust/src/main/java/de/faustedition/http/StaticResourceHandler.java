package de.faustedition.http;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import org.glassfish.grizzly.http.util.MimeType;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.model.Resource;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Deque;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class StaticResourceHandler implements Inflector<ContainerRequestContext, Response> {

    private final File rootFile;
    private final String resourceRoot;
    private final Date configurationDate = new Date();

    public static Resource create(String path, String root, final String resourceRoot) {
        final StaticResourceHandler handler = new StaticResourceHandler((root == null ? null : new File(root)), resourceRoot);
        final Resource.Builder rb = Resource.builder();
        rb.path(HTTP.normalizePath(path) + "/{path:.*}");
        rb.addMethod(HttpMethod.GET).handledBy(handler);
        return rb.build();
    }

    private StaticResourceHandler(File rootFile, String resourceRoot) {
        Preconditions.checkArgument(rootFile == null || rootFile.isDirectory(), rootFile);
        Preconditions.checkArgument(rootFile != null || resourceRoot != null);
        this.rootFile = rootFile;
        this.resourceRoot = resourceRoot;
    }

    @Override
    public Response apply(ContainerRequestContext containerRequestContext) {
        try {
            final String path = containerRequestContext.getUriInfo().getPathParameters().getFirst("path");

            // try to find resource in filesystem
            if (rootFile != null) {
                final Deque<String> pathDeque = HTTP.pathDeque(path);
                File file = rootFile;
                while (file != null && !pathDeque.isEmpty()) {
                    final File child = new File(file, pathDeque.remove());
                    if (child.exists() && child.getParentFile().equals(file)) {
                        file = child;
                    } else {
                        file = null;
                    }
                }
                if (file != null && file.isFile()) {
                    final Date lastModified = new Date(file.lastModified());
                    return Objects.firstNonNull(
                            containerRequestContext.getRequest().evaluatePreconditions(lastModified),
                            Response.ok(file)
                    ).type(MimeType.get(Files.getFileExtension(path), "application/octet-stream")).lastModified(lastModified).build();
                }
            }

            // try to find resource in classpath
            if (resourceRoot != null) {
                final URL resource = getClass().getResource(resourceRoot + "/" + path);
                if (resource != null) {
                    return Objects.firstNonNull(
                            containerRequestContext.getRequest().evaluatePreconditions(configurationDate),
                            Response.ok(resource.openStream())
                    ).type(MimeType.get(Files.getFileExtension(path), "application/octet-stream")).lastModified(configurationDate).build();
                }
            }

            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (IOException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
