package de.faustedition.model.service;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import de.faustedition.model.TEIDocument;
import de.faustedition.model.metadata.ManuscriptIdentifier;

public interface TEIDocumentManager {

	TEIDocument createDocument();

	void configure(TEIDocument teiDocument) throws SAXException, IOException;

	void setTitle(TEIDocument teiDocument, String title) throws SAXException, IOException;

	void serialize(TEIDocument document, OutputStream outStream) throws IOException, TransformerException;

	void setManuscriptIdentifier(TEIDocument teiDocument, ManuscriptIdentifier msIdentifier);

}
