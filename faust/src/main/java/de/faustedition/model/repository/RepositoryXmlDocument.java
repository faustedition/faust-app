package de.faustedition.model.repository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
import org.w3c.dom.Document;

import de.faustedition.util.XMLUtil;

public class RepositoryXmlDocument extends RepositoryFile {

	public RepositoryXmlDocument(Node node) {
		super(node);
	}

	public static RepositoryXmlDocument create(Node parentNode, String name, Document document) throws RepositoryException {
		RepositoryXmlDocument repositoryDocument = new RepositoryXmlDocument(parentNode.addNode(name, JcrConstants.NT_FILE));
		repositoryDocument.setDocument(document);
		return repositoryDocument;
	}

	public static RepositoryXmlDocument create(RepositoryObject parent, String name, Document document)
			throws RepositoryException {
		return create(parent.getNode(), name, document);
	}

	public Document getDocument() throws RepositoryException {
		InputStream contents = null;
		try {
			return XMLUtil.parse(getContents());
		} finally {
			IOUtils.closeQuietly(contents);
		}
	}

	public void setDocument(Document document) throws RepositoryException {
		setContent(new ByteArrayInputStream(XMLUtil.serialize(document, true)));
		setMimeType("application/xml");
		setEncoding("UTF-8");
		setLastModified(new Date());
	}
}
