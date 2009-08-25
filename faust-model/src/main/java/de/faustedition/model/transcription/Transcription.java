package de.faustedition.model.transcription;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.faustedition.model.store.AbstractContentObject;
import de.faustedition.util.ErrorUtil;
import de.faustedition.util.XMLUtil;

public class Transcription extends AbstractContentObject {

	public Transcription(String path) {
		super(path);
	}

	public static Collection<Transcription> find(Session session, Portfolio portfolio) throws RepositoryException {
		SortedSet<Transcription> transcriptions = new TreeSet<Transcription>();
		for (NodeIterator ni = portfolio.getNode(session).getNodes(); ni.hasNext();) {
			try {
				transcriptions.add(toTranscription(ni.nextNode()));
			} catch (IllegalArgumentException e) {
			}
		}
		return transcriptions;
	}

	public static Transcription get(Session session, Portfolio portfolio, String name) throws RepositoryException {
		return toTranscription(portfolio.getNode(session).getNode(name));
	}

	public static Transcription toTranscription(Node node) throws RepositoryException {
		if (!node.isNodeType("nt:file")) {
			throw new IllegalArgumentException(node.getPath());
		}
		return new Transcription(node.getPath());
	}

	public static Document retrieveDocument(Node node) throws RepositoryException, SAXException, IOException {
		return XMLUtil.build(new ByteArrayInputStream(retrieve(node)));
	}

	public static byte[] retrieve(Node node) throws RepositoryException {
		Value transcriptionData = node.getNode(JcrConstants.JCR_CONTENT).getProperty(JcrConstants.JCR_DATA).getValue();
		InputStream dataStream = null;
		try {
			return IOUtils.toByteArray(dataStream = transcriptionData.getStream());
		} catch (IOException e) {
			throw ErrorUtil.fatal("Error while reading transcription data", e);
		} finally {
			IOUtils.closeQuietly(dataStream);
		}
	}

	public String getPathInStore() {
		return StringUtils.removeStart(getPath(), TranscriptionStore.NAME + "/");
	}

}
