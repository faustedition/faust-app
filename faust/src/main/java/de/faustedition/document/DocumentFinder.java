package de.faustedition.document;

import org.restlet.resource.ServerResource;
import org.springframework.stereotype.Component;

import java.util.Deque;

@Component
public class DocumentFinder extends AbstractDocumentFinder {

	@Override
	protected ServerResource getResource(Document document, Deque<String> postfix, Deque<String> path) {
		return null;
	}

}
