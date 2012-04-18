package de.faustedition.document;

import java.util.Deque;
import java.util.logging.Logger;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.ServerResource;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.faustedition.xml.XMLStorage;

public class DocumentImageLinkFinder extends AbstractDocumentFinder {

	private final Provider<DocumentImageLinkResource> resources;

	@Inject
	public DocumentImageLinkFinder(XMLStorage xml, MaterialUnitManager documentManager, Provider<DocumentImageLinkResource> resources, Logger logger) {
		super(xml, documentManager, logger);
		this.resources = resources;
	}

	@Override
	protected ServerResource getResource(Document document, Deque<String> postfix) {
		DocumentImageLinkResource resource = resources.get();
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
