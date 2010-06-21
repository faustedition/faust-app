package de.faustedition.tei;

import static de.faustedition.tei.EncodedTextDocument.TEI_NS_URI;
import static de.faustedition.tei.EncodedTextDocument.TEI_SIG_GE_URI;
import static de.faustedition.tei.EncodedTextDocument.xpath;
import static de.faustedition.xml.NodeListIterable.singleResult;
import static de.faustedition.xml.XmlUtil.documentBuilder;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.faustedition.Log;
import de.faustedition.document.HandPropertiesManager;
import de.faustedition.xml.NodeListIterable;
import de.faustedition.xml.XmlStore;

@Service
public class EncodedTextDocumentBuilder implements Runnable {
	public static final String CSS_URI = "http://xml.faustedition.net/schema/faust-tei.css";

	@Value("#{config['tei.schema.url']}")
	private String teiSchemaUrl;

	@Autowired
	private HandPropertiesManager handProperties;

	@Autowired
	private GlyphManager glyphs;

	@Autowired
	private XmlStore xmlStore;

	public EncodedTextDocument create() {
		return addTemplate(EncodedTextDocument.create("TEI"));
	}

	public EncodedTextDocument addTemplate(EncodedTextDocument document) {
		Document dom = document.getDom();

		addProcessingInstructions(dom);
		addNamespaces(dom);
		addHeader(dom);

		handProperties.declareIn(dom);
		glyphs.declareIn(dom);

		addBody(dom);

		return document;
	}

	@Override
	public void run() {
		try {
			Log.LOGGER.info("Templating TEI documents");

			for (URI resource : xmlStore) {
				if (!xmlStore.isWitnessEncodingDocument(resource)) {
					continue;					
				}
				try {
					Log.LOGGER.debug("Templating TEI-XML in {}", resource.toString());
					EncodedTextDocument doc = new EncodedTextDocument((Document) xmlStore.get(resource));
					xmlStore.put(resource, addTemplate(doc).getDom());
				} catch (EncodedTextDocumentException e) {
					Log.LOGGER.warn("Resource '{}' is not a TEI document", resource.toString());
				}

			}
		} catch (IOException e) {
			Log.fatalError(e, "I/O error while templating TEI");
		}
	}

	private void addBody(Document dom) {
		Element tei = dom.getDocumentElement();
		Assert.isTrue("TEI".equals(tei.getLocalName()), "No <TEI/> root element");

		Element text = singleResult(xpath("./tei:text"), tei, Element.class);
		if (text == null) {
			text = dom.createElementNS(TEI_NS_URI, "text");
			tei.appendChild(text);

			Element body = dom.createElementNS(TEI_NS_URI, "body");
			text.appendChild(body);

			body.appendChild(dom.createElementNS(TEI_NS_URI, "p"));
		}

		if (singleResult(xpath("//ge:document"), dom, Element.class) == null) {
			Element document = dom.createElementNS(TEI_SIG_GE_URI, "ge:document");
			tei.insertBefore(document, text);

			Element surface = dom.createElementNS(TEI_NS_URI, "surface");
			document.appendChild(surface);

			Element zone = dom.createElementNS(TEI_NS_URI, "zone");
			surface.appendChild(zone);

			zone.appendChild(dom.createElementNS(TEI_SIG_GE_URI, "ge:line"));
		}
	}

	private void addHeader(Document dom) {
		if (singleResult(xpath("//tei:teiHeader"), dom, Element.class) != null) {
			return;
		}

		try {
			Document template = documentBuilder().parse(new ByteArrayInputStream(HEADER_TEMPLATE.getBytes("UTF-8")));
			Element root = dom.getDocumentElement();
			root.insertBefore(dom.importNode(template.getDocumentElement(), true), root.getFirstChild());
		} catch (SAXException e) {
			throw Log.fatalError(e, "XML error while adding TEI header template");
		} catch (IOException e) {
			throw Log.fatalError(e, "I/O error while adding TEI header template");
		}
	}

	private void addNamespaces(Document dom) {
		Element documentElement = dom.getDocumentElement();

		if (!documentElement.hasAttributeNS(XMLNS_ATTRIBUTE_NS_URI, "svg")) {
			documentElement.setAttributeNS(XMLNS_ATTRIBUTE_NS_URI, "xmlns:svg", EncodedTextDocument.SVG_NS_URI);
		}

		if (!documentElement.hasAttributeNS(XMLNS_ATTRIBUTE_NS_URI, "ge")) {
			documentElement.setAttributeNS(XMLNS_ATTRIBUTE_NS_URI, "xmlns:ge", EncodedTextDocument.TEI_SIG_GE_URI);
		}

		if (!documentElement.hasAttributeNS(XMLNS_ATTRIBUTE_NS_URI, "f")) {
			documentElement.setAttributeNS(XMLNS_ATTRIBUTE_NS_URI, "xmlns:f", EncodedTextDocument.FAUST_NS_URI);
		}
	}

	private void addProcessingInstructions(Document dom) {
		for (Node piNode : new NodeListIterable<Node>(xpath("/processing-instruction('xml-stylesheet')"), dom)) {
			dom.removeChild(piNode);
		}
		Node cssPi = dom.createProcessingInstruction("xml-stylesheet", String.format("href=\"%s\" type=\"text/css\"", CSS_URI));
		dom.insertBefore(cssPi, dom.getFirstChild());

		for (Node piNode : new NodeListIterable<Node>(xpath("/processing-instruction('oxygen')"), dom)) {
			dom.removeChild(piNode);
		}
		Node schemaPi = dom.createProcessingInstruction("oxygen", String.format("RNGSchema=\"%s\" type=\"xml\"", teiSchemaUrl));
		dom.insertBefore(schemaPi, cssPi);
	}

	private static final String HEADER_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"//
			+ "<teiHeader xmlns=\"http://www.tei-c.org/ns/1.0\"><fileDesc>"//
			+ "<titleStmt><title>Johann Wolfgang von Goethe: Faust</title></titleStmt>"//
			+ "<publicationStmt><publisher xml:id=\"edition\">Digitale Faust-Edition</publisher>"//
			+ "<pubPlace>Frankfurt am Main</pubPlace><date></date></publicationStmt>"//
			+ "<sourceDesc><p /></sourceDesc>"//
			+ "</fileDesc></teiHeader>";
}
