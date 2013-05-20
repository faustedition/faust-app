package de.faustedition.http;

import com.google.common.util.concurrent.AbstractIdleService;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.ResourceConfig;
import de.faustedition.Server;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.http.server.StaticHttpHandler;

import java.io.File;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Server.Component
public class HttpService extends AbstractIdleService {

    private HttpServer httpServer;

    public HttpService(ResourceConfig resourceConfig, int port, String contextPath, File staticDirectory) {
        this.httpServer = HttpServer.createSimpleServer(null, port);

        final HttpHandler httpHandler = ContainerFactory.createContainer(HttpHandler.class, resourceConfig);
        final ServerConfiguration config = httpServer.getServerConfiguration();
        config.addHttpHandler(new CustomStaticHttpHandler(staticDirectory.getPath(), contextPath + "/static"), contextPath + "/static/*");
        config.addHttpHandler(httpHandler, contextPath + "/*");
    }

    @Override
    protected void startUp() throws Exception {
        httpServer.start();
    }

    @Override
    protected void shutDown() throws Exception {
        httpServer.stop();
    }

    static class CustomStaticHttpHandler extends StaticHttpHandler {

        final String base;

        CustomStaticHttpHandler(String docRoot, String base) {
            super(docRoot);
            setFileCacheEnabled(false);
            this.base = base;
        }

        @Override
        protected String getRelativeURI(Request request) {
            String uri = request.getRequestURI();
            if (uri.contains("..")) {
                return null;
            }

            if (!base.isEmpty()) {
                if (!uri.startsWith(base)) {
                    return null;
                }

                uri = uri.substring(base.length());
            }

            return uri;
        }
    }

}