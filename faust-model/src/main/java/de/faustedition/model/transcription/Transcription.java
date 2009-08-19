package de.faustedition.model.transcription;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.faustedition.model.store.AbstractContentObject;
import de.faustedition.model.store.ContentStore;
import de.faustedition.model.store.ContentStoreCallback;
import de.faustedition.util.ErrorUtil;
import de.faustedition.util.XMLUtil;

public class Transcription extends AbstractContentObject {

	public Transcription(String path, String name) {
		super(path, name);
	}

	public Document getDocument(ContentStore contentStore) throws RepositoryException, SAXException, IOException {
		return XMLUtil.build(new ByteArrayInputStream(retrieve(contentStore)));
	}
	
	public byte[] retrieve(ContentStore contentStore) throws RepositoryException {
		return contentStore.execute(new ContentStoreCallback<byte[]>() {

			@Override
			public byte[] doInSession(Session session) throws RepositoryException {
				Node transcriptionNode = session.getRootNode().getNode(getPath());
				Value transcriptionData = transcriptionNode.getNode(JcrConstants.JCR_CONTENT).getProperty(JcrConstants.JCR_DATA).getValue();
				InputStream dataStream = null;
				try {
					return IOUtils.toByteArray(dataStream = transcriptionData.getStream());
				} catch (IOException e) {
					throw ErrorUtil.fatal("Error while reading transcription data", e);
				} finally {
					IOUtils.closeQuietly(dataStream);
				}
			}
		});

	}
}
