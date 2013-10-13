package de.faustedition.http;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractIdleService;
import de.faustedition.Configuration;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class HttpService extends AbstractIdleService {
    private static final Logger LOG = Logger.getLogger(HttpService.class.getName());

    private final HttpServer httpServer;

    private static final List<Object> STANDARD_COMPONENTS = Arrays.asList(
            new CrossOriginResourceSharingContainerFilter(),
            new IllegalArgumentExceptionMapper(),
            new JsonMappingExceptionMapper()
    );

    public HttpService(Configuration configuration, Object... components) {
        this(configuration, Arrays.asList(components));
    }

    public HttpService(Configuration configuration, Iterable<Object> components) {
        try {
            final ResourceConfig resourceConfig = new ResourceConfig();

            resourceConfig.registerResources(StaticResourceHandler.create("/static", Strings.emptyToNull(configuration.property("faust.static_root")), "/static"));

            final String yuiRoot = configuration.property("faust.yui_root");
            if (!yuiRoot.isEmpty()) {
                resourceConfig.registerResources(StaticResourceHandler.create(configuration.property("faust.yui_path"), yuiRoot, null));
            }

            final Map<String, Object> resourceMappings = Maps.newTreeMap();
            for (Object component : Iterables.concat(STANDARD_COMPONENTS, components)) {
                final Class<?> componentClass = component.getClass();
                if (componentClass.isAnnotationPresent(Path.class)) {
                    final String path = componentClass.getAnnotation(Path.class).value();
                    resourceConfig.register(component);
                    Preconditions.checkState(resourceMappings.put(path, component) == null, path);
                } else {
                    if (LOG.isLoggable(Level.CONFIG)) {
                        LOG.log(Level.CONFIG, "Adding {0} provider {1}", new Object[] {
                                (componentClass.isAnnotationPresent(Provider.class) ? "JAX-RS provider" : "component"),
                                component
                        });
                    }
                    resourceConfig.register(component);
                }
            }

            if (LOG.isLoggable(Level.CONFIG) && !resourceMappings.isEmpty()) {
                int maxPathLength = 0;
                for (String path : resourceMappings.keySet()) {
                    maxPathLength = Math.max(maxPathLength, path.length());
                }
                for (Map.Entry<String, Object> mapping : resourceMappings.entrySet()) {
                    LOG.log(Level.CONFIG, "JAX-RS Resource Mapping: " + Joiner.on(" => ").join(
                            Strings.padEnd(mapping.getKey(), maxPathLength, ' '),
                            mapping.getValue())
                    );
                }
            }

            final int port = Integer.parseInt(configuration.property("faust.http_port"));
            final String contextPath = configuration.property("faust.context_path");

            final URI uri = new URI("http", null, "localhost", port, contextPath, null, null);
            this.httpServer = GrizzlyHttpServerFactory.createHttpServer(uri, resourceConfig);
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    protected void startUp() throws Exception {
        httpServer.start();
    }

    @Override
    protected void shutDown() throws Exception {
        httpServer.shutdown();
    }
}
