package de.faustedition.tei;

import java.util.Deque;

import org.goddag4j.MultiRootedTree;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import de.faustedition.FaustURI;
import de.faustedition.text.Text;
import de.faustedition.text.TextGeneticJSONEnhancer;
import de.faustedition.text.TextManager;
import de.faustedition.transcript.GoddagTranscript;
import de.faustedition.transcript.GoddagTranscriptManager;
import de.faustedition.transcript.TranscriptType;
import de.faustedition.xml.XMLStorage;

@Component
public class GoddagFinder extends Finder {

	@Autowired
	private XMLStorage xml;

	@Autowired
	private Logger logger;

	@Autowired
	private GoddagTranscriptManager transcriptManager;

	@Autowired
	private TextManager textManager;

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public ServerResource find(Request request, Response response) {
		final Reference resourceRef = request.getResourceRef();
		final Deque<String> path = FaustURI.toPathDeque(resourceRef.getRelativeRef().getPath());

		logger.debug("Finding XML resource for " + path);
		FaustURI uri = null;
		try {
			uri = xml.walk(path);
		} catch (IllegalArgumentException e) {
			logger.debug("Parse error while resolving resource for " + path, e);
			return null;
		}
		if (uri == null) {
			return null;
		}

		final String uriPath = uri.getPath();
		final Form parameters = resourceRef.getQueryAsForm();
		GoddagResource resource = null;

		if (uriPath.startsWith("/text")) {
			logger.debug("Finding text for " + uri);
			final Text text = textManager.find(uri);
			if (text != null) {
				resource = getResource(parameters, uri, text.getTrees());
				resource.setEnhancer(applicationContext.getBean(TextGeneticJSONEnhancer.class));
			}
		} else if (uriPath.startsWith("/transcript")) {
			TranscriptType transcriptType = null;
			try {
				transcriptType = TranscriptType.valueOf(parameters.getFirstValue("type", "").toUpperCase());
			} catch (IllegalArgumentException e) {
			}

			logger.debug("Finding transcript for " + uri + (transcriptType == null ? "" : "[" + transcriptType + "]"));
			GoddagTranscript transcript = transcriptManager.find(uri, transcriptType);
			if (transcript != null) {
				resource = getResource(parameters, uri, transcript.getTrees());
				resource.setTranscriptType(transcript.getType());
			}
		}

		return resource;
	}

	protected GoddagResource getResource(Form parameters, FaustURI uri, MultiRootedTree trees) {
		final boolean snapshot = Boolean.valueOf(parameters.getFirstValue("snapshot", false));

		final GoddagResource resource = applicationContext.getBean(snapshot ? "snapshotGoddagResource" : "goddagResource", GoddagResource.class);
		resource.setSource(uri);
		resource.setTrees(trees);
		return resource;
	}
}
