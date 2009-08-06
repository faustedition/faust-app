package de.faustedition.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;

public class TranscriptionDocument extends TEIDocument {
	private Facsimile facsimile;
	private File file;

	protected TranscriptionDocument(Document document, File file) {
		super(document);
		this.file = file;
	}

	public static TranscriptionDocument create(File file) {
		return new TranscriptionDocument(createInstance().getDocument(), file);
	}

	public void save() throws IOException {
		FileOutputStream fileStream = null;
		try {
			serialize(fileStream = new FileOutputStream(file));
		} finally {
			IOUtils.closeQuietly(fileStream);
		}

	}

	public static TranscriptionDocument parse(File file) throws IOException, DocumentException {
		FileInputStream fileStream = null;
		try {
			return new TranscriptionDocument(TEIDocument.parse(fileStream = new FileInputStream(file)), file);
		} finally {
			IOUtils.closeQuietly(fileStream);
		}
	}

	public File getFile() {
		return file;
	}

	public Facsimile getFacsimile() {
		return facsimile;
	}

	public void setFacsimile(Facsimile facsimile) {
		this.facsimile = facsimile;
	}
}
