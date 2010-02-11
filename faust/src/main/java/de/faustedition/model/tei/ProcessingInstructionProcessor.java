package de.faustedition.model.tei;

import static de.faustedition.model.tei.EncodedTextDocumentValidator.SCHEMA_URI;
import static de.faustedition.model.xml.XPathUtil.xpath;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.faustedition.model.xml.NodeListIterable;

public class ProcessingInstructionProcessor implements EncodedTextDocumentProcessor {
	public static final String CSS_URI = "http://xml.faustedition.net/schema/faust-tei.css";
	private static final String CSS_ATTRS = "href=\"%s\" type=\"text/css\"";
	private static final String SCHEMA_ATTRS = "RNGSchema=\"%s\" type=\"compact\"";

	@Override
	public void process(EncodedTextDocument teiDocument) {
		Document dom = teiDocument.getDom();

		for (Node piNode : new NodeListIterable<Node>(xpath("/processing-instruction('xml-stylesheet')", null), dom)) {
			dom.removeChild(piNode);
		}
		Node cssPi = dom.createProcessingInstruction("xml-stylesheet", String.format(CSS_ATTRS, CSS_URI));
		dom.insertBefore(cssPi, dom.getFirstChild());

		for (Node piNode : new NodeListIterable<Node>(xpath("/processing-instruction('oxygen')", null), dom)) {
			dom.removeChild(piNode);
		}
		Node schemaPi = dom.createProcessingInstruction("oxygen", String.format(SCHEMA_ATTRS, SCHEMA_URI));
		dom.insertBefore(schemaPi, cssPi);

	}

}
