package de.faustedition.text;

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
public class TextFinder extends Finder {

	private final XMLStorage xml;
	private final Logger logger;
	private final TextManager textManager;
	private final Provider<TextResource> textResources;

	@Inject
	public TextFinder(XMLStorage xml, TextManager textManager, Provider<TextResource> textResources, Logger logger) {
		this.xml = xml;
		this.textManager = textManager;
		this.textResources = textResources;
		this.logger = logger;
	}

	@Override
	public ServerResource find(Request request, Response response) {
		final Deque<String> path = FaustURI.toPathDeque(request.getResourceRef().getRelativeRef().getPath());
		path.addFirst("text");

		logger.fine("Finding text resource for " + path);

		try {
			final FaustURI uri = xml.walk(path);
			if (uri == null) {
				return null;
			}

			logger.fine("Finding text for " + uri);
			final Text text = textManager.find(uri);
			if (text == null) {
				return null;
			}

			final TextResource resource = textResources.get();
			resource.setText(text);
			return resource;
		} catch (IllegalArgumentException e) {
			logger.log(Level.FINE, "Parse error while resolving text resource for " + path, e);
			return null;
		}
	}
}
