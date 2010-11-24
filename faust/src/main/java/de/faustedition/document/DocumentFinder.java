package de.faustedition.document;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.xml.XMLStorage;

@Singleton
public class DocumentFinder extends Finder {

	private final XMLStorage xml;
	private final MaterialUnitManager documentManager;
	private final Injector injector;
	private final Logger logger;

	@Inject
	public DocumentFinder(XMLStorage xml, MaterialUnitManager documentManager, Injector injector, Logger logger) {
		this.xml = xml;
		this.documentManager = documentManager;
		this.injector = injector;
		this.logger = logger;
	}

	@Override
	public ServerResource find(Request request, Response response) {
		final String path = request.getResourceRef().getRelativeRef().getPath().replaceAll("^/+", "").replaceAll("/+$", "");

		logger.fine("Finding document resource for '" + path + "'");
		final ArrayDeque<String> pathDeque = new ArrayDeque<String>(Arrays.asList(path.split("/+")));
		if (pathDeque.size() == 0) {
			return null;
		}

		FaustURI uri = new FaustURI(FaustAuthority.XML, "/document/");
		try {
			while (pathDeque.size() > 0) {
				FaustURI next = uri.resolve(pathDeque.pop());
				if (xml.isDirectory(next)) {
					uri = FaustURI.parse(next.toString() + "/");
					continue;
				}
				if (xml.isResource(next)) {
					uri = next;
					break;
				}
				return null;
			}
		} catch (IllegalArgumentException e) {
			logger.log(Level.FINE, "Parse error while resolving document resource for '" + path + "'", e);
			return null;
		}

		logger.fine("Finding document for " + uri);
		final Document document = documentManager.find(uri);
		if (document == null) {
			return null;
		}

		final DocumentResource resource = injector.getInstance(DocumentResource.class);
		resource.setDocument(document);
		return resource;
	}
}
