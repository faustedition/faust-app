package de.faustedition.http;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.core.DefaultLessCompiler;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;
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
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class StaticResourceHandler implements Inflector<ContainerRequestContext, Response> {
    private static final Logger LOG = Logger.getLogger(StaticResourceHandler.class.getName());

    private final File rootFile;
    private final String resourceRoot;

    private final Cache<String, TimestampedByteSource> cache = CacheBuilder.newBuilder()
            .weigher(BYTE_SOURCE_WEIGHER)
            .maximumWeight(10485760)
            .build();

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
            final String fileExtension = Files.getFileExtension(path);

            TimestampedByteSource source = cache.get(path, new Callable<TimestampedByteSource>() {
                @Override
                public TimestampedByteSource call() throws Exception {
                    TimestampedByteSource source = findSource(path);
                    if (source == null && "css".equals(fileExtension)) {
                        // try to find a LESS source for a CSS resource
                        TimestampedByteSource lessSource = findSource(path.replaceAll("\\.css$", ".less"));
                        if (lessSource != null) {
                            source = new de.faustedition.http.StaticResourceHandler.LessSource(lessSource);
                        }
                    }
                    if (source == null) {
                        throw new WebApplicationException(Response.Status.NOT_FOUND);
                    }
                    return source;
                }
            });
            final Date lastModified = source.lastModified();
            Response.ResponseBuilder rb = containerRequestContext.getRequest().evaluatePreconditions(lastModified);
            if (rb == null) {
                rb = Response.ok(source.byteSource().openBufferedStream());
            }
            return rb.type(MimeType.get(fileExtension, "application/octet-stream")).lastModified(lastModified).build();
        } catch (Throwable t) {
            Throwables.propagateIfInstanceOf(Throwables.getRootCause(t), WebApplicationException.class);
            throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    protected TimestampedByteSource findSource(String path) {
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
                return new FileSource(file);
            }
        }

        // try to find resource in classpath
        if (resourceRoot != null) {
            final URL resource = getClass().getResource(resourceRoot + "/" + path);
            if (resource != null) {
                return new ClasspathSource(resource);
            }
        }

        return null;
    }

    private static interface TimestampedByteSource {
        Date lastModified();

        ByteSource byteSource();
    }

    private static class FileSource implements TimestampedByteSource {

        private final File file;

        private FileSource(File file) {
            this.file = file;
        }

        @Override
        public Date lastModified() {
            return new Date(file.lastModified());
        }

        @Override
        public ByteSource byteSource() {
            return Files.asByteSource(file);
        }
    }

    private class ClasspathSource implements TimestampedByteSource {

        private final URL resource;

        private ClasspathSource(URL resource) {
            this.resource = resource;
        }

        @Override
        public Date lastModified() {
            return configurationDate;
        }

        @Override
        public ByteSource byteSource() {
            return Resources.asByteSource(resource);
        }
    }

    private static class LessSource implements TimestampedByteSource {

        private final TimestampedByteSource source;

        private LessSource(TimestampedByteSource source) {
            this.source = source;
        }

        @Override
        public Date lastModified() {
            return source.lastModified();
        }

        @Override
        public ByteSource byteSource() {
            try {
                final LessCompiler.CompilationResult compiledLess = new DefaultLessCompiler().compile(
                        source.byteSource().asCharSource(Charsets.UTF_8).read()
                );
                if (LOG.isLoggable(Level.WARNING)) {
                    for (LessCompiler.Problem lessProblem : compiledLess.getWarnings()) {
                        LOG.log(Level.WARNING, lessProblem.getMessage());
                    }
                }
                return ByteSource.wrap(compiledLess.getCss().getBytes(Charsets.UTF_8));
            } catch (Less4jException e) {
                throw Throwables.propagate(e);
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }
    }

    private static final Weigher<String, TimestampedByteSource> BYTE_SOURCE_WEIGHER = new Weigher<String, TimestampedByteSource>() {
        @Override
        public int weigh(String key, TimestampedByteSource value) {
            if (value instanceof FileSource) {
                return (int) ((FileSource)value).file.length();
            }
            try {
                return (int) value.byteSource().size();
            } catch (IOException e) {
            }
            return 0;
        }
    };


}