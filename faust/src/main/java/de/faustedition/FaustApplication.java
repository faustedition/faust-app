package de.faustedition;

import de.faustedition.document.ArchiveResource;
import de.faustedition.document.DocumentRouter;
import de.faustedition.facsimile.FacsimileFinder;
import de.faustedition.reasoning.InscriptionPrecedenceResource;
import de.faustedition.template.TemplateFinder;
import de.faustedition.transcript.SceneStatisticsResource;
import de.faustedition.transcript.TranscriptSourceResource;
import de.faustedition.transcript.TranscriptViewResource;
import de.faustedition.transcript.VerseStatisticsResource;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.CharacterSet;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.resource.Directory;
import org.restlet.resource.Finder;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class FaustApplication extends Application implements InitializingBean {
	@Autowired
	private Environment environment;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private DocumentRouter documentRouter;

    @Autowired
    private FacsimileFinder facsimileFinder;

	private File staticResourcePath;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.staticResourcePath = environment.getRequiredProperty("static.home", File.class);
		this.getMetadataService().setDefaultCharacterSet(CharacterSet.UTF_8);
	}

	@Override
	public Restlet createInboundRoot() {
		final Router router = new Router(getContext());

		router.setDefaultMatchingMode(Template.MODE_STARTS_WITH);

        	router.attach("document/", secured(transactional(documentRouter)));
        	router.attach("facsimile/", facsimileFinder);
		final Restlet inscriptionPrecendence = secured(transactional(contextResource(InscriptionPrecedenceResource.class)));
		router.attach("genesis/inscriptions/{part}/{act_scene}/{scene}/", inscriptionPrecendence);
		router.attach("genesis/inscriptions/{part}/{act_scene}/", inscriptionPrecendence);
		router.attach("transcript/by-scene/{part}", secured(transactional(contextResource(SceneStatisticsResource.class))));
		router.attach("transcript/by-verse/{from}/{to}", secured(transactional(contextResource(VerseStatisticsResource.class))));
		router.attach("transcript/source/{id}", secured(transactional(contextResource(TranscriptSourceResource.class))));
		router.attach("transcript/{id}", secured(transactional(contextResource(TranscriptViewResource.class))));
		router.attach("", EntryPageRedirectionResource.class, Template.MODE_EQUALS);
		router.attach("login", secured(new Finder(getContext().createChildContext(), EntryPageRedirectionResource.class)));
        return router;
	}

	private <T extends ServerResource> Restlet contextResource(Class<T> type) {
		return new ApplicationContextFinder<T>(applicationContext, type);
	}

	private Restlet secured(Restlet resource) {
		return resource;
	}

	private Restlet transactional(Restlet resource) {
		return resource;
	}

	public static class EntryPageRedirectionResource extends ServerResource {
		@Override
		protected Representation doHandle() throws ResourceException {
			getResponse().redirectTemporary(new Reference(getReference(), "archive/"));
			return null;
		}
	}

}