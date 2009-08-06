package de.faustedition.model.transcription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Portfolio extends TranscriptionStoreContents {

	protected Portfolio(TranscriptionStore store, TranscriptionStoreContents parent, String name) {
		super(store, parent, name);
	}

	public Collection<Transcription> findTranscriptions() throws TranscriptionStoreException {
		String[] documents = store.findDocuments(getPath());
		List<Transcription> transcriptions = new ArrayList<Transcription>(documents.length);
		for (String document : documents) {
			transcriptions.add(new Transcription(store, this, document));
		}
		return transcriptions;
	}
}
