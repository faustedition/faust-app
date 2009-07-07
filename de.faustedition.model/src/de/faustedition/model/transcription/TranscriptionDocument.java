package de.faustedition.model.transcription;

import org.w3c.dom.Document;

import de.faustedition.model.tei.TEIDocument;

public class TranscriptionDocument extends TEIDocument {
	private String path;

	public TranscriptionDocument(Document document, String path) {
		super(document);
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
