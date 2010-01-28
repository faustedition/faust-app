package de.faustedition.model.repository;

import static org.apache.jackrabbit.JcrConstants.JCR_CONTENT;
import static org.apache.jackrabbit.JcrConstants.JCR_DATA;
import static org.apache.jackrabbit.JcrConstants.NT_RESOURCE;

import java.io.InputStream;
import java.util.Date;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.JcrConstants;

public class RepositoryFile extends RepositoryObject {

	protected RepositoryFile(Node node) {
		super(node);
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

	public InputStream getContents() throws RepositoryException {
		return getContentNode().getProperty(JCR_DATA).getBinary().getStream();		
	}

	public void setContent(InputStream content) throws RepositoryException {
		getContentNode().setProperty(JCR_DATA, getValueFactory().createBinary(content));
	}
}
