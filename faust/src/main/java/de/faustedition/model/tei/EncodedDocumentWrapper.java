package de.faustedition.model.tei;

import java.io.InputStream;

import javax.jcr.RepositoryException;

import net.sf.practicalxml.XmlException;

import org.apache.commons.io.IOUtils;

import de.faustedition.model.repository.RepositoryFile;

public class EncodedDocumentWrapper {

	private final RepositoryFile file;

	public EncodedDocumentWrapper(RepositoryFile file) {
		this.file = file;
	}

	public EncodedDocument parse() throws RepositoryException, XmlException {
		InputStream contentStream = null;
		try {
			return EncodedDocument.parse(contentStream = file.getContent());
		} finally {
			IOUtils.closeQuietly(contentStream);
		}
	}


}
