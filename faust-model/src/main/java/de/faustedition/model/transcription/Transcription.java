package de.faustedition.model.transcription;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.faustedition.model.repository.DataRepository;
import de.faustedition.model.repository.RepositoryObjectBase;
import de.faustedition.util.XMLUtil;

public class Transcription extends RepositoryObjectBase {

	public Transcription(String path) {
		super(path);
	}

	public Document getDocument(Session session) throws RepositoryException, SAXException, IOException {
		return XMLUtil.build(new ByteArrayInputStream(DataRepository.loadFile(getNode(session))));
	}

	@Override
	public void load(Node node) throws RepositoryException {
		if (!node.isNodeType("nt:file")) {
			throw new IllegalArgumentException(node.getPath());
		}
	}

	public String getPathInStore() {
		return StringUtils.removeStart(getPath(), TranscriptionStore.NAME + "/");
	}

}
