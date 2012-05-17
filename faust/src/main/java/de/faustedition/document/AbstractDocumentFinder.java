package de.faustedition.document;

import de.faustedition.FaustURI;
import de.faustedition.xml.XMLStorage;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.Deque;

public abstract class AbstractDocumentFinder extends Finder {

	@Autowired
	protected ApplicationContext applicationContext;

	@Autowired
	protected XMLStorage xml;

	@Autowired
	protected MaterialUnitManager documentManager;

	@Autowired
	protected Logger logger;

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
			logger.debug("Parse error while resolving document resource for " +
				request.getResourceRef().getRelativeRef().getPath(), e);
			return null;
		}

	}

	protected DocumentPath getDocument(Request request) {
		final Deque<String> path = FaustURI.toPathDeque(request.getResourceRef().getRelativeRef().getPath());
		path.addFirst("document");

		logger.debug("Finding document resource for " + path);
		final FaustURI uri = xml.walk(path);

		logger.debug("Finding document for " + uri);
		return new DocumentPath(documentManager.find(uri), path);
	}

	protected abstract ServerResource getResource(Document document, Deque<String> postfix);


    protected class DocumentPath {
        public DocumentPath(Document document, Deque<String> path) {
            this.document = document;
            this.path = path;
        }

        public Document document;
        public Deque<String> path;
    }

}
