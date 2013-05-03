package de.faustedition.text;

import java.util.Deque;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import de.faustedition.FaustURI;
import de.faustedition.xml.XMLStorage;

@Component
public class TextFinder extends Finder {

	@Autowired
	private XMLStorage xml;

	@Autowired
	private Logger logger;

	@Autowired
	private TextManager textManager;

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public ServerResource find(Request request, Response response) {
		final Deque<String> path = FaustURI.toPathDeque(request.getResourceRef().getRelativeRef().getPath());
		path.addFirst("text");

		logger.debug("Finding text resource for " + path);

		try {
			final FaustURI uri = xml.walk(path);
			if (uri == null) {
				return null;
			}

			logger.debug("Finding text for " + uri);
			final Text text = textManager.find(uri);
			if (text == null) {
				return null;
			}

			final TextResource resource = applicationContext.getBean(TextResource.class);
			resource.setText(text);
			return resource;
		} catch (IllegalArgumentException e) {
			logger.debug("Parse error while resolving text resource for " + path, e);
			return null;
		}
	}
}
