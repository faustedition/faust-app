package de.faustedition.transcript;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.transcript.Transcript.Type;
import de.faustedition.xml.XMLStorage;

@Singleton
public class TranscriptFinder extends Finder {
	private final XMLStorage xml;
	private final TranscriptManager transcriptManager;
	private final Injector injector;
	private final Logger logger;

	@Inject
	public TranscriptFinder(XMLStorage xml, TranscriptManager transcriptManager, Injector injector, Logger logger) {
		this.xml = xml;
		this.transcriptManager = transcriptManager;
		this.injector = injector;
		this.logger = logger;
	}

	@Override
	public ServerResource find(Request request, Response response) {
		final Reference resourceRef = request.getResourceRef();
		final String path = resourceRef.getRelativeRef().getPath().replaceAll("^/+", "").replaceAll("/+$", "");
		logger.fine("Finding transcript resource for '" + path + "'");

		final ArrayDeque<String> pathDeque = new ArrayDeque<String>(Arrays.asList(path.split("/+")));
		if (pathDeque.size() == 0) {
			return null;
		}

		FaustURI uri = new FaustURI(FaustAuthority.XML, "/transcript/");
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
			logger.log(Level.FINE, "Parse error while resolving transcript resource for '" + path + "'", e);
			return null;
		}

		final Form parameters = resourceRef.getQueryAsForm();
		Type transcriptType = null;
		try {
			transcriptType = Type.valueOf(parameters.getFirstValue("type", "").toUpperCase());
		} catch (IllegalArgumentException e) {
		}

		logger.fine("Finding transcript for " + uri + (transcriptType == null ? "" : "[" + transcriptType + "]"));
		Transcript transcript = transcriptManager.find(uri, transcriptType);
		if (transcript == null) {
			return null;
		}

		final TranscriptResource resource = injector.getInstance(TranscriptResource.class);
		resource.setTranscript(transcript);
		return resource;
	}
}
