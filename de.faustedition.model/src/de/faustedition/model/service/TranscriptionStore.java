package de.faustedition.model.service;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import de.faustedition.model.TranscriptionDocument;
import de.faustedition.model.metadata.ManuscriptIdentifier;
import de.faustedition.model.xmldb.Collection;
import de.faustedition.model.xmldb.ExistException;

public interface TranscriptionStore {
	TranscriptionDocument createTranscription(Collection collection, String basename, ManuscriptIdentifier msIdentifier)
			throws IOException, SAXException, TransformerException, ExistException;
}
