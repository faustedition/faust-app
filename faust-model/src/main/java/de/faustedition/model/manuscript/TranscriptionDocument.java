package de.faustedition.model.manuscript;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import nu.xom.Document;
import nu.xom.Serializer;

public class TranscriptionDocument {
	private Document document;

	public TranscriptionDocument(Document document) {
		this.document = document;
	}

	public Document getDocument() {
		return document;
	}

	public byte[] serialize() throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		Serializer serializer = new Serializer(byteArrayOutputStream, "UTF-8");
		serializer.setIndent(4);
		serializer.setMaxLength(0);
		serializer.write(document);
		return byteArrayOutputStream.toByteArray();
	}
}
