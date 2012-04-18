package de.faustedition;

import static org.restlet.data.ChallengeScheme.HTTP_BASIC;

import java.util.List;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.resource.Directory;
import org.restlet.resource.Finder;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.security.Authenticator;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Role;
import org.restlet.security.RoleAuthorizer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import de.faustedition.collation.CollationFinder;
import de.faustedition.document.ArchiveRouter;
import de.faustedition.document.DocumentRouter;
import de.faustedition.genesis.GeneticGraphRouter;
import de.faustedition.security.LdapSecurityStore;
import de.faustedition.security.SecurityConstants;
import de.faustedition.structure.StructureFinder;
import de.faustedition.tei.GoddagFinder;
import de.faustedition.template.TemplateFinder;
import de.faustedition.text.TextFinder;
import de.faustedition.xml.XMLFinder;

@Singleton
public class FaustApplication extends Application {
	private final String staticResourcePath;
	private final TemplateFinder templateFinder;
	private final GeneticGraphRouter geneticGraphRouter;
	private final ArchiveRouter archiveRouter;
	private final DocumentRouter documentRouter;
	private final GoddagFinder goddagFinder;
	private final TextFinder textFinder;
	private final XMLFinder xmlFinder;
	private final StructureFinder structureFinder;
	private final LdapSecurityStore ldapSecurityStore;
	private final RuntimeMode runtimeMode;
	private final CollationFinder collationFinder;

	@Inject
	public FaustApplication(RuntimeMode runtimeMode,//
			@Named("static.home") String staticResourcePath,//
			ArchiveRouter archiveRouter,//
			DocumentRouter documentRouter,//
			GoddagFinder goddagFinder,//
			TextFinder textFinder,//
			XMLFinder xmlFinder,//
			StructureFinder structureFinder,//
			CollationFinder collationFinder,//
			TemplateFinder templateFinder,//
			GeneticGraphRouter geneticGraphRouter,//
			LdapSecurityStore ldapSecurityStore) {
		this.runtimeMode = runtimeMode;
		this.staticResourcePath = staticResourcePath;
		this.archiveRouter = archiveRouter;
		this.documentRouter = documentRouter;
		this.goddagFinder = goddagFinder;
		this.textFinder = textFinder;
		this.xmlFinder = xmlFinder;
		this.structureFinder = structureFinder;
		this.collationFinder = collationFinder;
		this.templateFinder = templateFinder;
		this.geneticGraphRouter = geneticGraphRouter;
		this.ldapSecurityStore = ldapSecurityStore;
	}

	@Override
	public Restlet createInboundRoot() {
		final Router router = new Router(getContext());

		router.setDefaultMatchingMode(Template.MODE_STARTS_WITH);

		router.attach("static/", new Directory(getContext().createChildContext(), "file://" + staticResourcePath + "/"));
		router.attach("project/", templateFinder);
		
		router.attach("archive/", secured(archiveRouter));
		router.attach("collation/", secured(collationFinder));
		router.attach("demo/", secured(templateFinder));
		router.attach("genesis/", secured(geneticGraphRouter));		
		router.attach("document/", secured(documentRouter));
		router.attach("goddag/", secured(goddagFinder));
		router.attach("text/", secured(textFinder));
		router.attach("structure/", secured(structureFinder));
		router.attach("xml/", secured(xmlFinder));
		router.attach("", EntryPageRedirectionResource.class, Template.MODE_EQUALS);
		router.attach("login", secured(new Finder(getContext().createChildContext(), EntryPageRedirectionResource.class)));

		switch (runtimeMode) {
		case DEVELOPMENT:
			final Filter dummyAuthenticator = new Filter() {
				@Override
				protected int beforeHandle(Request request, Response response) {
					final List<Role> roles = request.getClientInfo().getRoles();
					roles.add(SecurityConstants.ADMIN_ROLE);
					roles.add(SecurityConstants.EDITOR_ROLE);
					return super.beforeHandle(request, response);
				}
			};
			dummyAuthenticator.setNext(router);
			return dummyAuthenticator;
		default:
			final Authenticator authenticator = new ChallengeAuthenticator(getContext().createChildContext(), true,
					HTTP_BASIC, "faustedition.net", ldapSecurityStore);
			authenticator.setEnroler(ldapSecurityStore);
			authenticator.setNext(router);
			return authenticator;
		}
	}

	private Restlet secured(Restlet resource) {
		final RoleAuthorizer authorizer = new RoleAuthorizer();
		authorizer.getAuthorizedRoles().add(SecurityConstants.ADMIN_ROLE);
		authorizer.getAuthorizedRoles().add(SecurityConstants.EDITOR_ROLE);
		authorizer.setNext(resource);
		return authorizer;
	}

	public static class EntryPageRedirectionResource extends ServerResource {
		@Override
		protected Representation doHandle() throws ResourceException {
			getResponse().redirectTemporary(new Reference(getReference(), "project/about"));
			return null;
		}
	}

}