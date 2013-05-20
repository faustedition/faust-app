package de.faustedition.http;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractIdleService;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.ResourceConfig;
import de.faustedition.Server;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.http.server.StaticHttpHandler;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Singleton
@Server.Component
public class HttpService extends AbstractIdleService {

    private HttpServer httpServer;

    @Inject
    public HttpService(ResourceConfig resourceConfig,
                       @Named("server.port") int port,
                       @Named("ctx.path") String contextPath,
                       @Named("static.home") String staticPath) {

        Preconditions.checkArgument(new File(staticPath).isDirectory(), staticPath);

        this.httpServer = HttpServer.createSimpleServer(null, port);

        final HttpHandler httpHandler = ContainerFactory.createContainer(HttpHandler.class, resourceConfig);
        final ServerConfiguration config = httpServer.getServerConfiguration();
        config.addHttpHandler(new CustomStaticHttpHandler(staticPath, contextPath + "/static"), contextPath + "/static/*");
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