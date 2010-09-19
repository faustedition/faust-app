package de.faustedition;

import static org.restlet.routing.Template.MODE_STARTS_WITH;

import java.util.logging.Level;
import java.util.logging.Logger;

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

import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;

import de.faustedition.document.ArchiveResource;
import de.faustedition.facsimile.FacsimileProxyResource;
import de.faustedition.genesis.GenesisSampleChartResource;
import de.faustedition.genesis.GenesisSampleResource;
import de.faustedition.inject.ConfigurationModule;
import de.faustedition.inject.DataAccessModule;
import de.faustedition.inject.WebResourceModule;
import de.faustedition.security.DevelopmentAuthenticator;
import de.faustedition.template.TemplateRenderingResource;
import de.faustedition.transcript.TranscriptResource;

public class Server extends MainBase implements Runnable {

    public static void main(String[] args) {
        Server server = new Server();
        server.init(args);
        server.run();
    }

    @Override
    protected Module[] createModules() {
        return new Module[] { new ConfigurationModule(), new DataAccessModule(), new WebResourceModule(mode) };
    }

    @Override
    public void run() {
        final Logger logger = Logger.getLogger(getClass().getName());
        final Application app = new FaustApplication();
        try {
            logger.info("Starting Faust-Edition in " + mode + " mode");
            Component component = new Component();

            ClientList clients = component.getClients();
            clients.add(Protocol.FILE);
            clients.add(Protocol.HTTP);
            clients.add(Protocol.HTTPS);

            ServerList servers = component.getServers();
            switch (mode) {
            case PRODUCTION:
                servers.add(Protocol.AJP, 8089);
                break;
            case DEVELOPMENT:
                servers.add(Protocol.HTTP, 8080);
                break;
            }

            String contextPath = injector.getInstance(Key.get(String.class, Names.named("ctx.path")));
            logger.info("Mounting application under '/" + contextPath + "'");
            component.getDefaultHost().attach("/" + contextPath, app);
            component.start();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error while starting server", e);
            System.exit(1);
        }
    }

    private class FaustApplication extends Application {
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

            router.attach("document/styles", new GuiceFinder(Key.get(TemplateRenderingResource.class)));

            router.attach("genesis/", new GuiceFinder(Key.get(GenesisSampleResource.class)));
            router.attach("genesis/chart.png", GenesisSampleChartResource.class);

            router.attach("project/about", new GuiceFinder(Key.get(TemplateRenderingResource.class)));
            router.attach("project/imprint", new GuiceFinder(Key.get(TemplateRenderingResource.class)));
            router.attach("project/contact", new GuiceFinder(Key.get(TemplateRenderingResource.class)));

            router.attach("text/sample", new GuiceFinder(Key.get(TemplateRenderingResource.class)));

            router.attach(TranscriptResource.PATH + "/", new GuiceFinder(Key.get(TranscriptResource.class)), MODE_STARTS_WITH);

            if (mode == DeploymentMode.DEVELOPMENT) {
                router.attach("facsimile/iip", new GuiceFinder(Key.get(FacsimileProxyResource.class)));
            }

            Authenticator root = null;
            switch (mode) {
            default:
                root = new DevelopmentAuthenticator(getContext());
                root.setNext(router);
                break;
            }
            return root;
        }
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
