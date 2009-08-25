package de.faustedition.model.transformation;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.faustedition.model.metadata.MetadataBundle;
import de.faustedition.model.store.ContentObject;
import de.faustedition.model.store.ContentStore;
import de.faustedition.model.store.ContentStoreCallback;
import de.faustedition.model.transcription.Portfolio;
import de.faustedition.model.transcription.Repository;
import de.faustedition.model.transcription.Transcription;
import de.faustedition.util.ErrorUtil;
import de.faustedition.util.LoggingUtil;
import de.faustedition.util.XMLUtil;

public class MetadataCreationTransformer implements ContentTransformer {

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
	public void transformContent(ContentStore contentStore) throws RepositoryException {
		contentStore.execute(new ContentStoreCallback<Object>() {

			@Override
			public Object inStore(Session session) throws RepositoryException {
				try {
					for (Repository repository : Repository.find(session)) {
						addFaustMixin(session, repository);
						for (Portfolio portfolio : Portfolio.find(session, repository)) {
							addFaustMixin(session, portfolio);
							for (Transcription transcription : Transcription.find(session, portfolio)) {
								addFaustMixin(session, transcription);
								if (transcription.getName().startsWith("inventar_db_metadata")) {
									LoggingUtil.LOG.info(transcription.getPath());
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

	private void addFaustMixin(Session session, ContentObject contentObject) throws RepositoryException {
		javax.jcr.Node node = contentObject.getNode(session);
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

		Document metadataDocument = Transcription.retrieveDocument(metadataTranscription.getNode(session));
		NodeList valueLists = (NodeList) metadataListXPath.evaluate(metadataDocument, XPathConstants.NODESET);
		LoggingUtil.LOG.info(String.format("tei:list #: %d", valueLists.getLength()));
		for (int lc = 0; lc < valueLists.getLength(); lc++) {
			Element valueList = (Element) valueLists.item(lc);

			Map<String, String> metadata = new HashMap<String, String>();
			String metadataKey = null;
			NodeList listContents = valueList.getChildNodes();
			for (int lcc = 0; lcc < listContents.getLength(); lcc++) {
				Node listNode = listContents.item(lcc);
				if (!(listNode instanceof Element)) {
					continue;
				}
				Element listElement = (Element) listNode;

				if ("label".equals(listElement.getLocalName())) {
					metadataKey = StringUtils.trimToNull(listElement.getTextContent());
				}
				if ((metadataKey != null) && "item".equals(listElement.getLocalName())) {
					String newValue = listElement.getTextContent().trim();
					if (metadata.containsKey(metadataKey)) {
						newValue += ("\n" + metadata.get(metadataKey));
					}
					metadata.put(metadataKey, newValue);
				}
			}

			if (!metadata.isEmpty()) {
				MetadataBundle.create(session, portfolio, metadata);
			}
		}
	}

}
