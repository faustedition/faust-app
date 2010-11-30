package de.faustedition.structure;

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
public class StructureFinder extends Finder {

	private final XMLStorage xml;
	private final Injector injector;
	private final Logger logger;

	@Inject
	public StructureFinder(XMLStorage xml, Injector injector, Logger logger) {
		this.xml = xml;
		this.injector = injector;
		this.logger = logger;
	}

	@Override
	public ServerResource find(Request request, Response response) {
		final String path = request.getResourceRef().getRelativeRef().getPath().replaceAll("^/+", "").replaceAll("/+$", "");

		logger.fine("Finding structure resource for '" + path + "'");
		final ArrayDeque<String> pathDeque = new ArrayDeque<String>(Arrays.asList(path.split("/+")));
		if (pathDeque.size() == 0) {
			return null;
		}

		FaustURI uri = new FaustURI(FaustAuthority.XML, "/structure/archival/");
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
			logger.log(Level.FINE, "Parse error while resolving structure resource for '" + path + "'", e);
			return null;
		}


		final StructureResource resource = injector.getInstance(StructureResource.class);
		resource.setURI(uri);
		return resource;
	}
}
