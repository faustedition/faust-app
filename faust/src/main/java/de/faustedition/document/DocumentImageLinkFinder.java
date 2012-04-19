package de.faustedition.document;

import org.restlet.resource.ServerResource;
import org.springframework.stereotype.Component;

import java.util.Deque;

@Component
public class DocumentImageLinkFinder extends AbstractDocumentFinder {

	@Override
	protected ServerResource getResource(Document document, Deque<String> postfix) {
		DocumentImageLinkResource resource = applicationContext.getBean(DocumentImageLinkResource.class);
		if (postfix.size() != 1)
			return null;
		else {
			int pageNum;
			try {
				pageNum = Integer.parseInt(postfix.getFirst());
			} catch (NumberFormatException e) {
				return null;
			}
			resource.setDocument(document, pageNum);
		}
		return resource;
	}

}
