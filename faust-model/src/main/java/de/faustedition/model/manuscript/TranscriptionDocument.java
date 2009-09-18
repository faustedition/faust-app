package de.faustedition.model.manuscript;

import java.io.OutputStream;

import net.sf.practicalxml.DomUtil;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.base.Preconditions;

import de.faustedition.model.TEIDocument;
import de.faustedition.util.XMLUtil;

public class TranscriptionDocument {
	private Document document;

	public TranscriptionDocument(Document document) {
		this.document = document;
	}

	public Document getDocument() {
		return document;
	}

	public Element getTextElement() {
		Element textElement = DomUtil.getChild(document.getDocumentElement(), "text");
		Preconditions.checkNotNull(textElement);
		return textElement;
	}

	public Element getRevisionElement() {
		Element revisionElement = DomUtil.getChild(DomUtil.getChild(document.getDocumentElement(), "teiHeader"), "revisionDesc");
		Preconditions.checkNotNull(revisionElement);
		return revisionElement;
	}

	public void update(Transcription transcription) {
		transcription.setTextData(serializeFragment(getTextElement()));
		transcription.setRevisionData(serializeFragment(getRevisionElement()));
	}

	private byte[] serializeFragment(Element fragmentElement) {
		Document fragmentDataDocument = DomUtil.newDocument();
		fragmentDataDocument.appendChild(fragmentDataDocument.importNode(fragmentElement, true));
		return XMLUtil.serialize(fragmentDataDocument, false);

	}

	public boolean hasText() {
		for (Node textNode : TEIDocument.xpath(".//text()").evaluate(getTextElement())) {
			if (StringUtils.isNotBlank(textNode.getTextContent())) {
				return true;
			}
		}
		return false;
	}

	public void serialize(OutputStream stream, boolean indent) {
		XMLUtil.serialize(document, stream, indent);
	}
}
