package de.faustedition.model.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import de.faustedition.model.Folder;
import de.faustedition.model.TEIDocument;

public class MetadataDocument extends TEIDocument {
	public static final String FILE_NAME = "metadata.xml";

	private Folder folder;

	protected MetadataDocument(Document document, Folder folder) {
		super(document);
		this.folder = folder;
	}

	public Folder getFolder() {
		return folder;
	}

	public void save() throws IOException {
		FileOutputStream metadataStream = null;
		try {
			serialize(metadataStream = new FileOutputStream(getMetadataFile(folder)));
		} finally {
			IOUtils.closeQuietly(metadataStream);
		}
	}

	public static MetadataDocument createInstance(Folder folder) throws IOException, DocumentException {
		File metadataFile = getMetadataFile(folder);
		FileInputStream fileStream = null;
		try {
			Document document = exists(folder) ? parse(fileStream = new FileInputStream(metadataFile)) : createDocument();
			return new MetadataDocument(document, folder);
		} finally {
			IOUtils.closeQuietly(fileStream);
		}
	}

	public static File getMetadataFile(Folder folder) {
		return new File(folder.getFile(), FILE_NAME);
	}

	public static boolean exists(Folder folder) {
		return getMetadataFile(folder).isFile();
	}

	public static Document createDocument() {
		return createInstance().getDocument();
	}

}
