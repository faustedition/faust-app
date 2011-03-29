package de.faustedition.document;

import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

import de.faustedition.FaustURI;
import de.faustedition.xml.XMLStorage;

public abstract class AbstractDocumentFinder extends Finder {

	protected final XMLStorage xml;
	protected final MaterialUnitManager documentManager;
	protected final Logger logger;

	public AbstractDocumentFinder(XMLStorage xml, MaterialUnitManager documentManager, Logger logger) {
		this.xml = xml;
		this.documentManager = documentManager;
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

			return getResource(document);
		} catch (IllegalArgumentException e) {
			logger.log(Level.FINE, "Parse error while resolving document resource for " + path, e);
			return null;
		}

	}
	
	protected abstract ServerResource getResource(Document document);
}
