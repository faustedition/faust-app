package de.faustedition.xml;

import java.io.IOException;
import java.net.URI;
import java.util.SortedSet;

import org.w3c.dom.Document;

public interface XmlStore extends Iterable<URI> {
	final URI WITNESS_BASE = URI.create("Witness/");
	
	SortedSet<URI> list(URI uri) throws IOException;

	Document get(URI uri) throws IOException;

	void put(URI uri, Document document) throws IOException;

	void delete(URI uri) throws IOException;

	Document facsimileReferences();

	Document encodingStati();

	Document identifiers();
	
	boolean isWitnessEncodingDocument(URI uri) ;
	
	boolean isDocumentEncodingDocument(URI uri) ;
	
	boolean isTextEncodingDocument(URI uri);
}
