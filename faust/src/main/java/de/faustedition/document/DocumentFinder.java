package de.faustedition.document;

import org.restlet.resource.ServerResource;
import org.springframework.stereotype.Component;

import com.google.common.collect.Queues;

import de.faustedition.FaustURI;

import java.net.URI;
import java.util.Deque;

@Component
public class DocumentFinder extends AbstractDocumentFinder {

	@Override
	protected ServerResource getResource(Document document, Deque<String> postfix, Deque<String> path) {
		// Do not allow for arbitrary postfixes
		if (postfix.size() > 0)
			return null;
		final DocumentResource resource = applicationContext.getBean(DocumentResource.class);
		resource.setDocument(document);
		resource.setPath(FaustURI.fromDeque(path));
		
		return resource;
	}

}
