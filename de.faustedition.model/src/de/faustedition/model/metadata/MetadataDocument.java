package de.faustedition.model.metadata;

import org.w3c.dom.Document;

import de.faustedition.model.tei.TEIDocument;
import de.faustedition.model.xmldb.Collection;

public class MetadataDocument extends TEIDocument {
	public static final String FILE_NAME = "metadata.xml";

	private Collection collection;

	public MetadataDocument(Document document, Collection collection) {
		super(document);
		this.collection = collection;
	}

	public Collection getCollection() {
		return collection;
	}
}
