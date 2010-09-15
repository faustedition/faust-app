package de.faustedition;

import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.resource.Directory;
import org.restlet.resource.Finder;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.security.Authenticator;
import org.restlet.util.ClientList;
import org.restlet.util.ServerList;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Stage;
import com.google.inject.name.Names;

import de.faustedition.document.ArchiveResource;
import de.faustedition.facsimile.FacsimileProxyResource;
import de.faustedition.security.DevelopmentAuthenticator;
import de.faustedition.template.TemplateRenderingResource;

public class Server extends Application implements Runnable {
    private Mode mode = Mode.DEVELOPMENT;
    private Injector injector;

    public static void main(String[] args) {
        Server server = new Server();
        for (String arg : args) {
            if ("-production".equalsIgnoreCase(arg)) {
                server.mode = Mode.PRODUCTION;
            }
        }
        server.run();
    }

    @Override
    public void run() {
        try {
            getLogger().info("Starting Faust-Edition in " + mode + " mode");
            Component component = new Component();

            ClientList clients = component.getClients();
            clients.add(Protocol.FILE);
            clients.add(Protocol.HTTP);
            clients.add(Protocol.HTTPS);

            ServerList servers = component.getServers();
            switch (mode) {
            case PRODUCTION:
                injector = Guice.createInjector(Stage.PRODUCTION, new ServerModule());
                servers.add(Protocol.AJP, 8089);
                break;
            case DEVELOPMENT:
                injector = Guice.createInjector(Stage.DEVELOPMENT, new DevelopmentServerModule());
                servers.add(Protocol.HTTP, 8080);
                break;
            }

            String contextPath = injector.getInstance(Key.get(String.class, Names.named("ctx.path")));
            getLogger().info("Mounting application under '" + contextPath + "'");
            component.getDefaultHost().attach(contextPath, this);
            component.start();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Unexpected error while starting server", e);
            System.exit(1);
        }
    }

    @Override
    public Restlet createInboundRoot() {
        final Router router = new Router(getContext());

        router.attach("", EntryPageRedirectionResource.class);
        router.attach("login", new Finder(getContext(), EntryPageRedirectionResource.class));


        final String staticResourceDirectory = injector.getInstance(Key.get(String.class, Names.named("static.home")));
        router.attach("static", new Directory(getContext(), "file://" + staticResourceDirectory));

        Restlet archiveResource = new GuiceFinder(Key.get(ArchiveResource.class));
        router.attach("archive/", archiveResource);
        router.attach("archive/{id}", archiveResource);

        router.attach("project/about", new GuiceFinder(Key.get(TemplateRenderingResource.class)));
        router.attach("project/imprint", new GuiceFinder(Key.get(TemplateRenderingResource.class)));
        router.attach("project/contact", new GuiceFinder(Key.get(TemplateRenderingResource.class)));

        if (mode == Mode.DEVELOPMENT) {
            router.attach("facsimile/iip", new GuiceFinder(Key.get(FacsimileProxyResource.class)));
        }

        Authenticator root = null;
        switch (mode) {
        case DEVELOPMENT:
            root = new DevelopmentAuthenticator(getContext());
            root.setNext(router);
            break;
        }
        return root;
    }

    private enum Mode {
        PRODUCTION, DEVELOPMENT
    }

    private class GuiceFinder extends Finder {

        private final Key<? extends ServerResource> key;

        private GuiceFinder(Key<? extends ServerResource> key) {
            this.key = key;
        }

        @Override
        public ServerResource find(Request request, Response response) {
            return injector.getInstance(key);
        }
    }

    public static class EntryPageRedirectionResource extends ServerResource {
        @Override
        protected Representation doHandle() throws ResourceException {
            getResponse().redirectTemporary(new Reference(getReference(), "project/about"));
            return null;
        }
    }
}
