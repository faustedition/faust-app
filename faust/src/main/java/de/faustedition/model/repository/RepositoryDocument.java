package de.faustedition.model.repository;

import static org.apache.jackrabbit.JcrConstants.JCR_CONTENT;
import static org.apache.jackrabbit.JcrConstants.JCR_DATA;
import static org.apache.jackrabbit.JcrConstants.NT_RESOURCE;

import java.io.ByteArrayInputStream;
import java.util.Date;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.JcrConstants;

import de.faustedition.model.tei.EncodedTextDocument;
import de.faustedition.util.XMLUtil;

public class RepositoryDocument extends RepositoryObject {

	public RepositoryDocument(Node node) {
		super(node);
	}

	public static RepositoryDocument create(Node parentNode, String name, EncodedTextDocument document) throws RepositoryException {
		RepositoryDocument repositoryDocument = new RepositoryDocument(parentNode.addNode(name, JcrConstants.NT_FILE));
		repositoryDocument.setDocument(document);
		return repositoryDocument;
	}

	public static RepositoryDocument create(RepositoryObject parent, String name, EncodedTextDocument document) throws RepositoryException {
		return create(parent.getNode(), name, document);
	}

	public EncodedTextDocument getDocument() throws RepositoryException {
		return EncodedTextDocument.parse(getContentNode().getProperty(JCR_DATA).getBinary().getStream());
	}

	public void setDocument(EncodedTextDocument document) throws RepositoryException {
		byte[] content = XMLUtil.serialize(document.getDocument(), false);
		getContentNode().setProperty(JCR_DATA, getValueFactory().createBinary(new ByteArrayInputStream(content)));
		setMimeType("application/xml");
		setEncoding("UTF-8");
		setLastModified(new Date());
	}

	public Date getLastModified() throws RepositoryException {
		return getContentNode().getProperty(JcrConstants.JCR_LASTMODIFIED).getDate().getTime();
	}
	
	public void setLastModified(Date lastModified) throws RepositoryException {
		getContentNode().setProperty(JcrConstants.JCR_LASTMODIFIED, lastModified.getTime());
	}
		
	public void setMimeType(String mimeType) throws RepositoryException {
		getContentNode().setProperty(JcrConstants.JCR_MIMETYPE, mimeType);
	}

	public String getMimeType() throws RepositoryException {
		return getContentNode().getProperty(JcrConstants.JCR_MIMETYPE).getString();
	}

	public void setEncoding(String encoding) throws RepositoryException {
		getContentNode().setProperty(JcrConstants.JCR_ENCODING, encoding);
	}

	public String getEncoding() throws RepositoryException {
		return getContentNode().getProperty(JcrConstants.JCR_ENCODING).getString();
	}

	public Node getContentNode() throws RepositoryException {
		return node.hasNode(JCR_CONTENT) ? node.getNode(JCR_CONTENT) : node.addNode(JCR_CONTENT, NT_RESOURCE);
	}
}
