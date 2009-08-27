package de.faustedition.model.repository.transform;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.faustedition.model.metadata.MetadataBundle;
import de.faustedition.model.metadata.MetadataValue;
import de.faustedition.model.repository.DataRepository;
import de.faustedition.model.repository.DataRepositoryTemplate;
import de.faustedition.model.repository.RepositoryObject;
import de.faustedition.model.transcription.Portfolio;
import de.faustedition.model.transcription.Repository;
import de.faustedition.model.transcription.Transcription;
import de.faustedition.model.transcription.TranscriptionStore;
import de.faustedition.util.ErrorUtil;
import de.faustedition.util.XMLUtil;

public class MetadataCreationTransformer implements RepositoryTransformer {

	private XPathExpression metadataListXPath;

	public MetadataCreationTransformer() {
		super();
		try {
			metadataListXPath = XMLUtil.xpath().compile("//tei:body/tei:list[@type='gloss']");
		} catch (XPathExpressionException e) {
			throw ErrorUtil.fatal("XPath error while transforming metadata", e);
		}
	}

	@Override
	public void transformData(DataRepository dataRepository) throws RepositoryException {
		dataRepository.execute(new DataRepositoryTemplate<Object>() {

			@Override
			public Object doInSession(Session session) throws RepositoryException {
				try {
					for (Repository repository : TranscriptionStore.get(session).find(session, Repository.class)) {
						addFaustMixin(session, repository);
						for (Portfolio portfolio : repository.find(session, Portfolio.class)) {
							addFaustMixin(session, portfolio);
							for (Transcription transcription : portfolio.find(session, Transcription.class)) {
								addFaustMixin(session, transcription);
								if (transcription.getName().startsWith("inventar_db_metadata")) {
									createMetadata(session, portfolio, transcription);
									// contentStore.delete(transcription);
								}
							}
						}
					}
					session.save();
					return null;
				} catch (SAXException e) {
					throw ErrorUtil.fatal("XML error while transforming metadata", e);
				} catch (IOException e) {
					throw ErrorUtil.fatal("I/O error while transforming metadata", e);
				} catch (XPathExpressionException e) {
					throw ErrorUtil.fatal("XPath error while transforming metadata", e);
				}
			}

		});
	}

	private void addFaustMixin(Session session, RepositoryObject repositoryObject) throws RepositoryException {
		javax.jcr.Node node = repositoryObject.getNode(session);
		if (!node.isNodeType("faust:annotated")) {
			node.addMixin("faust:annotated");
			node.save();
		}
	}

	private void createMetadata(Session session, Portfolio portfolio, Transcription metadataTranscription) throws RepositoryException, SAXException, IOException, XPathExpressionException {
		javax.jcr.Node node = portfolio.getNode(session);
		for (NodeIterator ni = node.getNodes(MetadataBundle.NODE_NAME); ni.hasNext();) {
			ni.nextNode().remove();
		}

		NodeList valueLists = (NodeList) metadataListXPath.evaluate(metadataTranscription.getDocument(session), XPathConstants.NODESET);
		for (int lc = 0; lc < valueLists.getLength(); lc++) {
			Element valueList = (Element) valueLists.item(lc);

			Map<String, MetadataValue> metadata = new HashMap<String, MetadataValue>();
			String metadataField = null;
			NodeList listContents = valueList.getChildNodes();
			for (int lcc = 0; lcc < listContents.getLength(); lcc++) {
				Node listNode = listContents.item(lcc);
				if (!(listNode instanceof Element)) {
					continue;
				}
				Element listElement = (Element) listNode;

				if ("label".equals(listElement.getLocalName())) {
					metadataField = StringUtils.trimToNull(listElement.getTextContent());
				}
				if ((metadataField != null) && "item".equals(listElement.getLocalName())) {
					String metadataValue = listElement.getTextContent().trim();
					if (metadata.containsKey(metadataField)) {
						MetadataValue old = metadata.get(metadataField);
						metadata.put(metadataField, new MetadataValue(metadataField, old.getValue() + "\n" + metadataValue));
					} else {
						metadata.put(metadataField, new MetadataValue(metadataField, metadataValue));
					}
				}
			}

			if (!metadata.isEmpty()) {
				MetadataBundle.create(session, portfolio, metadata);
			}
		}
	}

}
