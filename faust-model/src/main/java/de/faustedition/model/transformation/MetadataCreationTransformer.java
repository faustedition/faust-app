package de.faustedition.model.transformation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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

public class MetadataCreationTransformer implements ContentTransformer {

	@Override
	public void transformContent(ContentStore contentStore) throws RepositoryException {
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression listXPath = xpath.compile("//body/list[@type='gloss']");
			for (Repository repository : contentStore.findTranscriptionStore().findRepositories(contentStore)) {
				addFaustMixin(contentStore, repository);
				for (Portfolio portfolio : repository.findPortfolios(contentStore)) {
					addFaustMixin(contentStore, portfolio);
					for (Transcription transcription : portfolio.findTranscriptions(contentStore)) {
						addFaustMixin(contentStore, transcription);
						if (transcription.getName().startsWith("inventar_db_metadata")) {
							LoggingUtil.LOG.info(transcription.getPath());

							for (MetadataBundle metadataBundle : contentStore.list(portfolio, MetadataBundle.class)) {
								contentStore.delete(metadataBundle);
							}

							Document metadataDocument = transcription.getDocument(contentStore);
							NodeList valueLists = (NodeList) listXPath.evaluate(metadataDocument, XPathConstants.NODESET);
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
									
									if ("label".equals(listElement.getNodeName())) {
										metadataKey = StringUtils.trimToNull(listElement.getTextContent());
									}
									if ((metadataKey != null) && "item".equals(listElement.getNodeName())) {
										String newValue = listElement.getTextContent().trim();
										if (metadata.containsKey(metadataKey)) {
											newValue += ("\n" + metadata.get(metadataKey));
										}
										metadata.put(metadataKey, newValue);
									}
								}

								if (!metadata.isEmpty()) {
									MetadataBundle metadataBundle = new MetadataBundle(portfolio);
									metadataBundle.setValues(metadata);
									contentStore.save(metadataBundle);
								}
							}
							// contentStore.delete(transcription);
						}
					}
				}
			}
		} catch (SAXException e) {
			throw ErrorUtil.fatal("XML error while transforming metadata", e);
		} catch (IOException e) {
			throw ErrorUtil.fatal("I/O error while transforming metadata", e);
		} catch (XPathExpressionException e) {
			throw ErrorUtil.fatal("XPath error while transforming metadata", e);
		}

	}

	private void addFaustMixin(ContentStore contentStore, final ContentObject contentObject) throws RepositoryException {
		contentStore.execute(new ContentStoreCallback<Object>() {

			@Override
			public Object doInSession(Session session) throws RepositoryException {
				javax.jcr.Node node = contentObject.getNode(session);
				for (NodeType mixin : node.getMixinNodeTypes()) {
					if ("faust:annotated".equals(mixin.getName())) {
						return null;
					}
				}

				node.addMixin("faust:annotated");
				session.save();
				return null;
			}
		});
	}

}
