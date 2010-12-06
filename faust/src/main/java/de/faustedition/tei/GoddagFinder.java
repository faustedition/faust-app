package de.faustedition.tei;

import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import de.faustedition.FaustURI;
import de.faustedition.genesis.GeneticRelationManager;
import de.faustedition.text.Text;
import de.faustedition.text.TextGeneticJSONEnhancer;
import de.faustedition.text.TextManager;
import de.faustedition.transcript.Transcript;
import de.faustedition.transcript.Transcript.Type;
import de.faustedition.transcript.TranscriptManager;
import de.faustedition.xml.XMLStorage;

@Singleton
public class GoddagFinder extends Finder {

	private final XMLStorage xml;
	private final Logger logger;
	private final TranscriptManager transcriptManager;
	private final TextManager textManager;
	private final Provider<GoddagResource> goddagResources;
	private final Provider<TextGeneticJSONEnhancer> textGeneticEnhancers;

	@Inject
	public GoddagFinder(XMLStorage xml, Provider<GoddagResource> goddagResources, TranscriptManager transcriptManager,
			TextManager textManager, Provider<TextGeneticJSONEnhancer> textGeneticEnhancers, Logger logger) {
		this.xml = xml;
		this.goddagResources = goddagResources;
		this.transcriptManager = transcriptManager;
		this.textManager = textManager;
		this.textGeneticEnhancers = textGeneticEnhancers;
		this.logger = logger;
	}

	@Override
	public ServerResource find(Request request, Response response) {
		final Reference resourceRef = request.getResourceRef();
		final Deque<String> path = FaustURI.toPathDeque(resourceRef.getRelativeRef().getPath());

		logger.fine("Finding XML resource for " + path);
		FaustURI uri = null;
		try {
			uri = xml.walk(path);
		} catch (IllegalArgumentException e) {
			logger.log(Level.FINE, "Parse error while resolving resource for " + path, e);
			return null;
		}
		if (uri == null) {
			return null;
		}

		final String uriPath = uri.getPath();
		GoddagResource resource = null;

		if (uriPath.startsWith("/text")) {
			logger.fine("Finding text for " + uri);
			final Text text = textManager.find(uri);
			if (text != null) {
				resource = goddagResources.get();
				resource.setTrees(text.getTrees());
				resource.setEnhancer(textGeneticEnhancers.get());
			}
		} else if (uriPath.startsWith("/transcript")) {
			final Form parameters = resourceRef.getQueryAsForm();
			Type transcriptType = null;
			try {
				transcriptType = Type.valueOf(parameters.getFirstValue("type", "").toUpperCase());
			} catch (IllegalArgumentException e) {
			}

			logger.fine("Finding transcript for " + uri + (transcriptType == null ? "" : "[" + transcriptType + "]"));
			Transcript transcript = transcriptManager.find(uri, transcriptType);
			if (transcript != null) {
				resource = goddagResources.get();
				resource.setTrees(transcript.getTrees());
			}
		}

		return resource;

	}
}
