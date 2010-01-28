package de.faustedition.model.tei;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ProcessingInstructionProcessor implements EncodedTextDocumentProcessor {

	private static final String CSS_ATTRS = "href=\"%s\" type=\"text/css\"";
	private static final String SCHEMA_ATTRS = "RNGSchema=\"%s\" type=\"compact\"";
	private final String schemaUrl;
	private final String stylesheetUrl;

	public ProcessingInstructionProcessor(String baseUrl) {
		this.schemaUrl = baseUrl + "schema/faust.rnc";
		this.stylesheetUrl = baseUrl + "schema/faust.css";
	}

	@Override
	public void process(EncodedTextDocument teiDocument) {
		Document dom = teiDocument.getDom();

		for (Node piNode : teiDocument.xpath("/processing-instruction('xml-stylesheet')")) {
			dom.removeChild(piNode);
		}
		Node cssPi = dom.createProcessingInstruction("xml-stylesheet", String.format(CSS_ATTRS, stylesheetUrl));
		dom.insertBefore(cssPi, dom.getFirstChild());

		for (Node piNode : teiDocument.xpath("/processing-instruction('oxygen')")) {
			dom.removeChild(piNode);
		}
		Node schemaPi = dom.createProcessingInstruction("oxygen", String.format(SCHEMA_ATTRS, schemaUrl));
		dom.insertBefore(schemaPi, cssPi);

	}

}
