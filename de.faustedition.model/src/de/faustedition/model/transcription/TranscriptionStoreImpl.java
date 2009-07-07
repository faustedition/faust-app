package de.faustedition.model.transcription;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import de.faustedition.model.metadata.ManuscriptIdentifier;
import de.faustedition.model.tei.TEIDocument;
import de.faustedition.model.tei.TEIDocumentManager;
import de.faustedition.model.xmldb.Collection;
import de.faustedition.model.xmldb.ExistDatabase;
import de.faustedition.model.xmldb.ExistException;

@Service("transcriptionStore")
public class TranscriptionStoreImpl implements TranscriptionStore {
	@Autowired
	private TEIDocumentManager teiDocumentManager;

	@Autowired
	private ExistDatabase existDatabase;

	public TranscriptionDocument createTranscription(Collection collection, String name, ManuscriptIdentifier msIdentifier)
			throws IOException, SAXException, TransformerException, ExistException {
		TEIDocument teiDocument = teiDocumentManager.createDocument();
		teiDocumentManager.setTitle(teiDocument, name);
		teiDocumentManager.setManuscriptIdentifier(teiDocument, msIdentifier);
		
		String transcriptionPath = collection.createEntryPath(name + ".xml");		
		existDatabase.put(transcriptionPath, teiDocument.getDocument());
		return new TranscriptionDocument(teiDocument.getDocument(), transcriptionPath);
	}

}
