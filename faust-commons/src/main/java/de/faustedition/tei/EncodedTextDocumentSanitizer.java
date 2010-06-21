package de.faustedition.tei;

import static de.faustedition.tei.EncodedTextDocument.xpath;
import static de.faustedition.xml.NodeListIterable.singleResult;
import static de.faustedition.xml.XmlUtil.hasText;

import java.io.IOException;
import java.net.URI;

import javax.xml.XMLConstants;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.faustedition.Log;
import de.faustedition.xml.XmlStore;
import de.faustedition.xml.XmlUtil;

@Service
public class EncodedTextDocumentSanitizer implements InitializingBean, Runnable {
	@Autowired
	private XmlStore xmlStore;

	private Templates teiSigCleanup;
	private Templates text2Doc;
	
	@Override
	public void run() {
		try {
			Log.LOGGER.info("Sanitizing TEI documents");

			Transformer cleanup = teiSigCleanup.newTransformer();

			for (URI resource : xmlStore) {
				Log.LOGGER.debug(resource.toASCIIString());
				if (!xmlStore.isWitnessEncodingDocument(resource)) {
					continue;
				}
				try {
					Log.LOGGER.debug("Sanitizing TEI-XML in {}", resource.toString());
					EncodedTextDocument tei = new EncodedTextDocument((Document) xmlStore.get(resource));

					Document dom = XmlUtil.documentBuilder().newDocument();
					cleanup.transform(new DOMSource(tei.getDom()), new DOMResult(dom));

					try {
						if (xmlStore.isDocumentEncodingDocument(resource) && //
								(Integer.parseInt(FilenameUtils.getBaseName(resource.getPath())) != 1) && //
								!hasText(singleResult(xpath("//ge:document"), dom, Element.class)) && //
								hasText(singleResult(xpath("//tei:text"), dom, Element.class))) {
							Log.LOGGER.info("Transforming text- to document-encoding in " + resource.toString());
							convertText2DocumentEncoding(dom);
						}
					} catch (NumberFormatException e) {
					}

					xmlStore.put(resource, dom);
				} catch (EncodedTextDocumentException e) {
					Log.LOGGER.warn("Resource '{}' is not a TEI document", resource.toString());
				} catch (TransformerException e) {
					Log.LOGGER.warn("Resource '{}' could not be cleaned up", resource.toString());
				}

			}
		} catch (IOException e) {
			Log.fatalError(e, "I/O error while sanitizing TEI");
		} catch (TransformerException e) {
			Log.fatalError(e, "XSLT error while sanitizing TEI");
		}
	}

	private void convertText2DocumentEncoding(Document dom) throws TransformerException {
		Element document = singleResult(xpath("//ge:document"), dom, Element.class);
		document.setAttributeNS(XMLConstants.XML_NS_URI, "xml:id", "converted");
		XmlUtil.removeChildren(document);
		
		text2Doc.newTransformer().transform(new DOMSource(singleResult(xpath("//tei:text"), dom, Element.class)), //
				new DOMResult(document));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		TransformerFactory tf = XmlUtil.transformerFactory();
		teiSigCleanup = tf.newTemplates(new StreamSource(getClass().getResourceAsStream("tei-sig-cleanup.xsl")));
		text2Doc = tf.newTemplates(new StreamSource(getClass().getResourceAsStream("text2doc.xsl")));
	}

}
