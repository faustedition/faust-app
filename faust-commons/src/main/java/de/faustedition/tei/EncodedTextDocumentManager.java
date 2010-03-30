package de.faustedition.tei;

import static de.faustedition.tei.EncodedTextDocument.TEI_NS_URI;
import static de.faustedition.tei.EncodedTextDocument.xpath;
import static de.faustedition.tei.EncodedTextDocumentValidator.SCHEMA_URI;
import static de.faustedition.xml.NodeListIterable.singleResult;
import static de.faustedition.xml.XmlUtil.documentBuilder;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.faustedition.ErrorUtil;
import de.faustedition.xml.NodeListIterable;

@Service
public class EncodedTextDocumentManager {
	public static final String CSS_URI = "http://xml.faustedition.net/schema/faust-tei.css";
	private static final String CSS_ATTRS = "href=\"%s\" type=\"text/css\"";
	private static final String SCHEMA_ATTRS = "RNGSchema=\"%s\" type=\"compact\"";
	private static final Resource HEADER_TEMPLATE_RESOURCE = new ClassPathResource("header-template.xml",
			EncodedTextDocumentManager.class);
	private static final Resource HAND_NOTES_RESOURCE = new ClassPathResource("hand-notes.xml",
			EncodedTextDocumentManager.class);
	private static final Resource CHAR_DECL_RESOURCE = new ClassPathResource("character-declarations.xml",
			EncodedTextDocumentManager.class);

	public EncodedTextDocument create() {
		return process(EncodedTextDocument.create("TEI"));
	}

	public EncodedTextDocument process(EncodedTextDocument document) {
		Document dom = document.getDom();

		addProcessingInstructions(dom);
		addNamespaces(dom);
		addHeader(dom);
		addHandNotes(dom);
		addCharacterDeclarations(dom);

		return document;
	}

	private void addCharacterDeclarations(Document dom) {
		Element header = singleResult(xpath("//tei:teiHeader"), dom, Element.class);
		Assert.notNull(header, "No TEI header in document");

		Element encodingDesc = singleResult(xpath("./tei:encodingDesc"), header, Element.class);
		if (encodingDesc == null) {
			encodingDesc = dom.createElementNS(TEI_NS_URI, "encodingDesc");
			Element revisionDesc = singleResult(xpath("./tei:revisionDesc"), header, Element.class);
			header.insertBefore(encodingDesc, revisionDesc);
		}

		Element charDecl = singleResult(xpath("./tei:charDecl"), encodingDesc, Element.class);
		Node insertBefore = null;
		if (charDecl != null) {
			insertBefore = charDecl.getNextSibling();
			encodingDesc.removeChild(charDecl);
		}

		try {
			Element template = documentBuilder().parse(CHAR_DECL_RESOURCE.getInputStream()).getDocumentElement();
			encodingDesc.insertBefore(dom.importNode(template, true), insertBefore);
		} catch (SAXException e) {
			throw ErrorUtil.fatal(e, "XML error while adding character declarations");
		} catch (IOException e) {
			throw ErrorUtil.fatal(e, "XML error while adding character declarations");
		}
	}

	private void addHandNotes(Document dom) {
		Element header = singleResult(xpath("//tei:teiHeader"), dom, Element.class);
		Assert.notNull(header, "No TEI header in document");

		Element profileDesc = singleResult(xpath("./tei:profileDesc"), header, Element.class);
		if (profileDesc == null) {
			profileDesc = dom.createElementNS(TEI_NS_URI, "profileDesc");
			Element revisionDesc = singleResult(xpath("./tei:revisionDesc"), header, Element.class);
			header.insertBefore(profileDesc, revisionDesc);
		}

		Element handNotes = singleResult(xpath("./tei:handNotes"), profileDesc, Element.class);
		Node insertBefore = null;
		if (handNotes != null) {
			insertBefore = handNotes.getNextSibling();
			profileDesc.removeChild(handNotes);
		}

		try {
			Element template = documentBuilder().parse(HAND_NOTES_RESOURCE.getInputStream()).getDocumentElement();
			profileDesc.insertBefore(dom.importNode(template, true), insertBefore);
		} catch (SAXException e) {
			throw ErrorUtil.fatal(e, "XML error while adding hand notes");
		} catch (IOException e) {
			throw ErrorUtil.fatal(e, "I/O error while adding hand notes");
		}
	}

	private void addHeader(Document dom) {
		if (singleResult(xpath("//tei:teiHeader"), dom, Element.class) != null) {
			return;
		}

		try {
			Element template = documentBuilder().parse(HEADER_TEMPLATE_RESOURCE.getInputStream()).getDocumentElement();
			Element root = dom.getDocumentElement();
			root.insertBefore(dom.importNode(template, true), root.getFirstChild());
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
		Node cssPi = dom.createProcessingInstruction("xml-stylesheet", String.format(CSS_ATTRS, CSS_URI));
		dom.insertBefore(cssPi, dom.getFirstChild());

		for (Node piNode : new NodeListIterable<Node>(xpath("/processing-instruction('oxygen')"), dom)) {
			dom.removeChild(piNode);
		}
		Node schemaPi = dom.createProcessingInstruction("oxygen", String.format(SCHEMA_ATTRS, SCHEMA_URI));
		dom.insertBefore(schemaPi, cssPi);
	}
}
