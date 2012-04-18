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

	protected class DocumentPath {
		public DocumentPath(Document document, Deque<String> path) {
			this.document = document; 
			this.path = path;
		}
		public Document document;
		public Deque<String> path;
	}
	
	@Override
	public ServerResource find(Request request, Response response) {

		try {
			
			final DocumentPath docPath = getDocument(request);
			final Document document = docPath.document;
			final Deque<String> remainder = docPath.path;
			
		if (document == null) {
				return null;
			}


		return getResource(document, remainder);


		} catch (IllegalArgumentException e) {
			logger.log(Level.FINE, "Parse error while resolving document resource for " + 
					request.getResourceRef().getRelativeRef().getPath(), e);
			return null;
		}

	}

	protected DocumentPath getDocument(Request request) {
		final Deque<String> path = FaustURI.toPathDeque(request.getResourceRef().getRelativeRef().getPath());
		path.addFirst("document");

		logger.fine("Finding document resource for " + path);
		
			final FaustURI uri = xml.walk(path);
			Document document;
			
			if (uri == null) 
				document = null;
			
			logger.fine("Finding document for " + uri);
			document = documentManager.find(uri);
			return new DocumentPath(document, path);
	}
	
	
	protected abstract ServerResource getResource(Document document, Deque<String> postfix);
	
}
