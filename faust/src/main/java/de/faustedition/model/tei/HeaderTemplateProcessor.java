package de.faustedition.model.tei;

import static de.faustedition.model.tei.EncodedTextDocument.TEI_NS_URI;
import static de.faustedition.model.tei.EncodedTextDocument.xpath;
import static de.faustedition.model.xmldb.NodeListIterable.singleResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class HeaderTemplateProcessor implements EncodedTextDocumentProcessor {

	private static final String DEFAULT_TITLE = "Johann Wolfgang von Goethe: Faust";

	@Override
	public void process(EncodedTextDocument teiDocument) {
		Document dom = teiDocument.getDom();

		Element teiHeader = singleResult(xpath("//tei:teiHeader"), dom, Element.class);
		if (teiHeader == null) {
			Element document = dom.getDocumentElement();
			document.insertBefore(teiHeader = dom.createElementNS(TEI_NS_URI, "teiHeader"), document.getFirstChild());
		}

		Element profileDesc = singleResult(xpath("//tei:teiHeader/tei:profileDesc"), dom, Element.class);
		if (profileDesc == null) {
			teiHeader.appendChild(profileDesc = dom.createElementNS(TEI_NS_URI, "profileDesc"));
			profileDesc.appendChild(dom.createElementNS(TEI_NS_URI, "p"));
		}

		Element encodingDesc = singleResult(xpath("//tei:teiHeader/tei:encodingDesc"), dom, Element.class);
		if (encodingDesc == null) {
			teiHeader.insertBefore(encodingDesc = dom.createElementNS(TEI_NS_URI, "encodingDesc"), profileDesc);
			encodingDesc.appendChild(dom.createElementNS(TEI_NS_URI, "p"));
		}

		Element fileDesc = singleResult(xpath("//tei:teiHeader/tei:fileDesc"), dom, Element.class);
		if (fileDesc == null) {
			teiHeader.insertBefore(fileDesc = dom.createElementNS(TEI_NS_URI, "fileDesc"), encodingDesc);
		}

		Element sourceDesc = singleResult(xpath("//tei:teiHeader/tei:fileDesc/tei:sourceDesc"), dom, Element.class);
		if (sourceDesc == null) {
			fileDesc.appendChild(sourceDesc = dom.createElementNS(TEI_NS_URI, "sourceDesc"));
			sourceDesc.appendChild(dom.createElementNS(TEI_NS_URI, "p"));
		}

		Element pubStmt = singleResult(xpath("//tei:teiHeader/tei:fileDesc/tei:publicationStmt"), dom, Element.class);
		if (pubStmt == null) {
			fileDesc.insertBefore(pubStmt = dom.createElementNS(TEI_NS_URI, "publicationStmt"), sourceDesc);
			pubStmt.appendChild(dom.createElementNS(TEI_NS_URI, "p"));
		}

		Element titleStmt = singleResult(xpath("//tei:teiHeader/tei:fileDesc/tei:titleStmt"), dom, Element.class);
		if (titleStmt == null) {
			fileDesc.insertBefore(titleStmt = dom.createElementNS(TEI_NS_URI, "titleStmt"), fileDesc.getFirstChild());
		}

		Element title = singleResult(xpath("//tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title"), dom, Element.class);
		if (title == null) {
			titleStmt.insertBefore(title = dom.createElementNS(TEI_NS_URI, "title"), titleStmt.getFirstChild());
			title.setTextContent(DEFAULT_TITLE);
		}

	}
}
