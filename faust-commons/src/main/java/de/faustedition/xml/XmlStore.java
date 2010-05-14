package de.faustedition.xml;

import java.io.IOException;
import java.net.URI;
import java.util.SortedSet;

import org.w3c.dom.Document;

public interface XmlStore {

	SortedSet<URI> contents() throws IOException;;

	SortedSet<URI> list(URI uri) throws IOException;;

	Document get(URI uri) throws IOException;

	void put(URI uri, Document document) throws IOException;

	void delete(URI uri) throws IOException;

	Document facsimileReferences();

	Document encodingStati();

	Document identifiers();
}
