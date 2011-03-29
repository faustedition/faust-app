package de.faustedition.document;

import java.util.logging.Logger;

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
	protected ServerResource getResource(Document document) {
		DocumentImageLinkResource resource = resources.get();
		resource.setDocument(document);
		return resource;
	}

}
