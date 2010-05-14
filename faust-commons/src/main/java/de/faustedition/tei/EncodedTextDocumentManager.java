package de.faustedition.tei;

import static de.faustedition.tei.EncodedTextDocument.xpath;
import static de.faustedition.xml.NodeListIterable.singleResult;
import static de.faustedition.xml.XmlUtil.documentBuilder;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.faustedition.ErrorUtil;
import de.faustedition.document.HandPropertiesManager;
import de.faustedition.xml.NodeListIterable;

@Service
public class EncodedTextDocumentManager {
	public static final String SCHEMA_URI = "http://www.faustedition.net/schema/faust-tei.rng";
	public static final String CSS_URI = "http://www.faustedition.net/css/faust-tei.css";

	@Autowired
	private HandPropertiesManager handProperties;

	@Autowired
	private GlyphManager glyphs;

	public EncodedTextDocument create() {
		return process(EncodedTextDocument.create("TEI"));
	}

	public EncodedTextDocument process(EncodedTextDocument document) {
		Document dom = document.getDom();

		addProcessingInstructions(dom);
		addNamespaces(dom);
		addHeader(dom);
		
		handProperties.declareIn(dom);
		glyphs.declareIn(dom);

		return document;
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
			throw ErrorUtil.fatal(e, "XML error while adding TEI header template");
		} catch (IOException e) {
			throw ErrorUtil.fatal(e, "I/O error while adding TEI header template");
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
		Node schemaPi = dom.createProcessingInstruction("oxygen", String.format("RNGSchema=\"%s\" type=\"xml\"", SCHEMA_URI));
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
