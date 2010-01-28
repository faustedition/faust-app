package de.faustedition.model.tei;

import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NamespaceDeclarationProcessor implements EncodedTextDocumentProcessor {

	@Override
	public void process(EncodedTextDocument teiDocument) {
		Document dom = teiDocument.getDom();
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

}
