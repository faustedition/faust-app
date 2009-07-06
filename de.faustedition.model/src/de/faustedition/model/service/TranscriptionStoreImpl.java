package de.faustedition.model.service;

import java.io.IOException;
import java.util.logging.Level;

import javax.xml.transform.TransformerException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import de.faustedition.model.TEIDocument;
import de.faustedition.model.TranscriptionDocument;
import de.faustedition.model.metadata.ManuscriptIdentifier;
import de.faustedition.model.xmldb.Collection;
import de.faustedition.model.xmldb.ExistException;
import de.faustedition.model.xmldb.ExistXmlStorage;
import de.faustedition.util.LoggingUtil;

@Service("transcriptionStore")
public class TranscriptionStoreImpl implements TranscriptionStore {
	@Autowired
	private TEIDocumentManager teiDocumentManager;

	@Autowired
	private ExistXmlStorage existXmlStorage;

	public TranscriptionDocument createTranscription(Collection collection, String name, ManuscriptIdentifier msIdentifier)
			throws IOException, SAXException, TransformerException, ExistException {
		LoggingUtil.log(Level.INFO, String.format("Transcription: [%s] in [%s]", name, collection.getPath()));

		TEIDocument teiDocument = teiDocumentManager.createDocument();
		teiDocumentManager.setTitle(teiDocument, name);
		teiDocumentManager.setManuscriptIdentifier(teiDocument, msIdentifier);
		
		String transcriptionPath = collection.createEntryPath(name + ".xml");		
		existXmlStorage.put(transcriptionPath, teiDocument.getDocument());
		return new TranscriptionDocument(teiDocument.getDocument(), transcriptionPath);
	}

}
