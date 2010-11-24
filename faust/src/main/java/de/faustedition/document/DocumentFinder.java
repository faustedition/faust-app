package de.faustedition.document;

import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import de.faustedition.FaustURI;
import de.faustedition.xml.XMLStorage;

@Singleton
public class DocumentFinder extends Finder {

	private final XMLStorage xml;
	private final MaterialUnitManager documentManager;
	private final Provider<DocumentResource> documentResources;
	private final Logger logger;

	@Inject
	public DocumentFinder(XMLStorage xml, MaterialUnitManager documentManager, Provider<DocumentResource> documentResources, Logger logger) {
		this.xml = xml;
		this.documentManager = documentManager;
		this.documentResources = documentResources;
		this.logger = logger;
	}

	@Override
	public ServerResource find(Request request, Response response) {
		final Deque<String> path = FaustURI.toPathDeque(request.getResourceRef().getRelativeRef().getPath());
		path.addFirst("document");

		logger.fine("Finding document resource for " + path);

		try {
			final FaustURI uri = xml.walk(path);
			if (uri == null) {
				return null;
			}
			
			logger.fine("Finding document for " + uri);
			final Document document = documentManager.find(uri);
			if (document == null) {
				return null;
			}

			final DocumentResource resource = documentResources.get();
			resource.setDocument(document);
			return resource;
		} catch (IllegalArgumentException e) {
			logger.log(Level.FINE, "Parse error while resolving document resource for " + path, e);
			return null;
		}

	}
}
