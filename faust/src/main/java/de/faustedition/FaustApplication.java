package de.faustedition;

import static org.restlet.data.ChallengeScheme.HTTP_BASIC;

import java.util.List;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CharacterSet;
import org.restlet.data.Reference;
import org.restlet.engine.application.Encoder;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import de.faustedition.collation.CollationFinder;
import de.faustedition.db.TransactionFilter;
import de.faustedition.document.ArchiveRouter;
import de.faustedition.document.DocumentRouter;
import de.faustedition.facsimile.FacsimileFinder;
import de.faustedition.genesis.GeneticGraphRouter;
import de.faustedition.reasoning.InscriptionPrecedenceResource;
import de.faustedition.search.SearchResource;
import de.faustedition.security.LdapSecurityStore;
import de.faustedition.security.SecurityConstants;
import de.faustedition.structure.StructureFinder;
import de.faustedition.tei.GoddagFinder;
import de.faustedition.template.TemplateFinder;
import de.faustedition.text.TextFinder;
import de.faustedition.transcript.SceneStatisticsResource;
import de.faustedition.transcript.TranscriptSourceResource;
import de.faustedition.transcript.TranscriptViewResource;
import de.faustedition.transcript.VerseStatisticsResource;
import de.faustedition.xml.XMLFinder;

@Component
public class FaustApplication extends Application implements InitializingBean {
	@Autowired
	private Environment environment;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private TemplateFinder templateFinder;

	@Autowired
	private GeneticGraphRouter geneticGraphRouter;

	@Autowired
	private ComboResourceFinder comboResourceFinder;

	@Autowired
	private ArchiveRouter archiveRouter;

	@Autowired
	private DocumentRouter documentRouter;

	@Autowired
	private GoddagFinder goddagFinder;

    	@Autowired
    	private FacsimileFinder facsimileFinder;

	@Autowired
	private TextFinder textFinder;

	@Autowired
	private XMLFinder xmlFinder;

	@Autowired
	private StructureFinder structureFinder;

	@Autowired
	private LdapSecurityStore ldapSecurityStore;

	@Autowired
	private CollationFinder collationFinder;

	private String staticResourcePath;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.staticResourcePath = environment.getRequiredProperty("static.home");
		this.getMetadataService().setDefaultCharacterSet(CharacterSet.UTF_8);
	}

	@Override
	public Restlet createInboundRoot() {
		final Router router = new Router(getContext());

		router.setDefaultMatchingMode(Template.MODE_STARTS_WITH);

		router.attach("archive/", secured(transactional(archiveRouter)));
		router.attach("collation/", secured(transactional(collationFinder)));
        	router.attach("demo/", secured(transactional(templateFinder)));
        	router.attach("document/", secured(transactional(documentRouter)));
        	router.attach("facsimile/", facsimileFinder);
		final Restlet inscriptionPrecendence = secured(transactional(contextResource(InscriptionPrecedenceResource.class)));
		router.attach("genesis/inscriptions/{part}/{act_scene}/{scene}/", inscriptionPrecendence);
		router.attach("genesis/inscriptions/{part}/{act_scene}/", inscriptionPrecendence);
		router.attach("genesis/work", secured(templateFinder));
		router.attach("genesis/app/", secured(templateFinder));
		router.attach("genesis/", secured(transactional(geneticGraphRouter)));
		
		router.attach("goddag/", secured(transactional(goddagFinder)));
        	router.attach("project/", templateFinder);
        	router.attach("static/", new Directory(getContext().createChildContext(), "file://" + staticResourcePath + "/"));
		router.attach("search/{term}", secured(transactional(contextResource(SearchResource.class))));
        	router.attach("structure/", secured(transactional(structureFinder)));
        	router.attach("text/", secured(transactional(textFinder)));
		router.attach("transcript/by-scene/{part}", secured(transactional(contextResource(SceneStatisticsResource.class))));
		router.attach("transcript/by-verse/{from}/{to}", secured(transactional(contextResource(VerseStatisticsResource.class))));
		router.attach("transcript/source/{id}", secured(transactional(contextResource(TranscriptSourceResource.class))));
		router.attach("transcript/{id}", secured(transactional(contextResource(TranscriptViewResource.class))));
		router.attach("xml/", secured(xmlFinder));
		router.attach("", EntryPageRedirectionResource.class, Template.MODE_EQUALS);
		router.attach("login", secured(new Finder(getContext().createChildContext(), EntryPageRedirectionResource.class)));
		router.attach("resources", comboResourceFinder);

		if (environment.acceptsProfiles("development", "test")) {
			final Filter dummyAuthenticator = new Filter() {
				@Override
				protected int beforeHandle(Request request, Response response) {
					final List<Role> roles = request.getClientInfo().getRoles();
					roles.add(SecurityConstants.ADMIN_ROLE);
					roles.add(SecurityConstants.EDITOR_ROLE);
					roles.add(SecurityConstants.EXTERNAL_ROLE);
					return super.beforeHandle(request, response);
				}
			};
			dummyAuthenticator.setNext(router);

			return dummyAuthenticator;
		} else {
			final Authenticator authenticator = new ChallengeAuthenticator(getContext().createChildContext(), true,
				HTTP_BASIC, "faustedition.net", ldapSecurityStore);
			authenticator.setEnroler(ldapSecurityStore);
			authenticator.setNext(router);

			final Encoder encoder = new Encoder(getContext());
			encoder.setNext(authenticator);

			return encoder;
		}
	}

	private <T extends ServerResource> Restlet contextResource(Class<T> type) {
		return new ApplicationContextFinder<T>(applicationContext, type);
	}

	private Restlet secured(Restlet resource) {
		final RoleAuthorizer authorizer = new RoleAuthorizer();
		authorizer.getAuthorizedRoles().add(SecurityConstants.ADMIN_ROLE);
		authorizer.getAuthorizedRoles().add(SecurityConstants.EDITOR_ROLE);
		authorizer.getAuthorizedRoles().add(SecurityConstants.EXTERNAL_ROLE);
		authorizer.setNext(resource);
		return authorizer;
	}

	private Restlet transactional(Restlet resource) {
		return new TransactionFilter(getContext(), resource, transactionManager);
	}

	public static class EntryPageRedirectionResource extends ServerResource {
		@Override
		protected Representation doHandle() throws ResourceException {
			getResponse().redirectTemporary(new Reference(getReference(), "genesis/work"));
			return null;
		}
	}

}