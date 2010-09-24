package de.faustedition;

import static org.restlet.data.ChallengeScheme.HTTP_BASIC;
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
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.security.Authenticator;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.RoleAuthorizer;
import org.restlet.util.ClientList;

import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;

import de.faustedition.document.ArchiveResource;
import de.faustedition.document.DocumentFinder;
import de.faustedition.genesis.GenesisSampleChartResource;
import de.faustedition.genesis.GenesisSampleResource;
import de.faustedition.inject.ConfigurationModule;
import de.faustedition.inject.DataAccessModule;
import de.faustedition.inject.WebResourceModule;
import de.faustedition.security.LdapSecurityStore;
import de.faustedition.security.SecurityConstants;
import de.faustedition.template.TemplateRenderingResource;
import de.faustedition.transcript.TranscriptFinder;
import de.faustedition.transcript.TranscriptResource;

public class Server extends MainBase implements Runnable {

    public static void main(String[] args) {
        Server server = new Server();
        server.init(args);
        server.run();
    }

    @Override
    protected Module[] createModules() {
        return new Module[] { new ConfigurationModule(), new DataAccessModule(), new WebResourceModule() };
    }

    @Override
    public void run() {
        final Logger logger = Logger.getLogger(getClass().getName());
        final Application app = new FaustApplication();
        try {
            logger.info("Starting Faust-Edition in " + mode + " mode");

            final Component component = new Component();
            ClientList clients = component.getClients();
            clients.add(Protocol.FILE);
            clients.add(Protocol.HTTP);
            clients.add(Protocol.HTTPS);

            switch (mode) {
            case PRODUCTION:
                component.getServers().add(Protocol.AJP, 8089);
                break;
            case DEVELOPMENT:
                component.getServers().add(Protocol.HTTP, 8080);
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

            final String staticResourceDirectory = injector.getInstance(Key.get(String.class, Names.named("static.home")));
            router.attach("static", new Directory(getContext(), "file://" + staticResourceDirectory));

            Restlet archiveResource = authorized(new GuiceFinder(Key.get(ArchiveResource.class)));
            router.attach("archive/", archiveResource);
            router.attach("archive/{id}", archiveResource);

            router.attach("document/styles", new GuiceFinder(Key.get(TemplateRenderingResource.class)));
            router.attach("document/", authorized(injector.getInstance(DocumentFinder.class)), MODE_STARTS_WITH);

            router.attach("genesis/", authorized(new GuiceFinder(Key.get(GenesisSampleResource.class))));
            router.attach("genesis/chart.png", authorized(router.createFinder(GenesisSampleChartResource.class)));

            router.attach("project/about", new GuiceFinder(Key.get(TemplateRenderingResource.class)));
            router.attach("project/imprint", new GuiceFinder(Key.get(TemplateRenderingResource.class)));
            router.attach("project/contact", new GuiceFinder(Key.get(TemplateRenderingResource.class)));

            router.attach("text/sample", authorized(new GuiceFinder(Key.get(TemplateRenderingResource.class))));

            router.attach("transcript/", authorized(injector.getInstance(TranscriptFinder.class)), MODE_STARTS_WITH);

            if (mode == DeploymentMode.DEVELOPMENT) {
                Filter assignAllRolesFilter = new Filter() {
                    @Override
                    protected int beforeHandle(Request request, Response response) {
                        request.getClientInfo().getRoles().add(SecurityConstants.ADMIN_ROLE);
                        request.getClientInfo().getRoles().add(SecurityConstants.EDITOR_ROLE);
                        return super.beforeHandle(request, response);
                    }
                };
                assignAllRolesFilter.setNext(router);
                return assignAllRolesFilter;
            } else {
                final LdapSecurityStore ldap = injector.getInstance(LdapSecurityStore.class);

                final Authenticator nonOptional = new ChallengeAuthenticator(getContext(), false, HTTP_BASIC, "faustedition.net",
                        ldap);
                nonOptional.setEnroler(ldap);
                nonOptional.setNext(authorized(new Finder(getContext(), EntryPageRedirectionResource.class)));
                router.attach("login", nonOptional);

                final Authenticator optional = new ChallengeAuthenticator(getContext(), true, HTTP_BASIC, "faustedition.net", ldap);
                optional.setEnroler(ldap);
                optional.setNext(router);

                return optional;
            }
        }

        private Restlet authorized(Restlet resource) {
            final RoleAuthorizer authorizer = new RoleAuthorizer();
            authorizer.getAuthorizedRoles().add(SecurityConstants.ADMIN_ROLE);
            authorizer.getAuthorizedRoles().add(SecurityConstants.EDITOR_ROLE);
            authorizer.setNext(resource);
            return authorizer;
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
