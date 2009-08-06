package de.faustedition.model.transcription;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.faustedition.util.XMLUtil;

public class Transcription extends TranscriptionStoreContents {

	protected Transcription(TranscriptionStore store, TranscriptionStoreContents parent, String name) {
		super(store, parent, name);
	}

	public Document retrieve() throws TranscriptionStoreException, SAXException, IOException {
		return XMLUtil.build(new ByteArrayInputStream(store.retrieve(getPath())));
	}
}
