package de.faustedition.model.manuscript;

import java.io.OutputStream;

import net.sf.practicalxml.DomUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Preconditions;

import de.faustedition.util.XMLUtil;

public class TranscriptionDocument
{
	private Document document;

	public TranscriptionDocument(Document document)
	{
		this.document = document;
	}

	public Document getDocument()
	{
		return document;
	}

	public Element getTextElement()
	{
		Element textElement = DomUtil.getChild(document.getDocumentElement(), "text");
		Preconditions.checkNotNull(textElement);
		return textElement;
	}

	public Element getRevisionElement()
	{
		Element revisionElement = DomUtil.getChild(DomUtil.getChild(document.getDocumentElement(), "teiHeader"), "revisionDesc");
		Preconditions.checkNotNull(revisionElement);
		return revisionElement;
	}

	public boolean hasText()
	{
		return XMLUtil.hasText(getTextElement());
	}

	public void update(Transcription transcription)
	{
		transcription.setTextData(serializeFragment(getTextElement()));
		transcription.setRevisionData(serializeFragment(getRevisionElement()));
	}

	private byte[] serializeFragment(Element fragmentElement)
	{
		Document fragmentDataDocument = DomUtil.newDocument();
		fragmentDataDocument.appendChild(fragmentDataDocument.importNode(fragmentElement, true));
		return XMLUtil.serialize(fragmentDataDocument, false);

	}

	public void serialize(OutputStream stream, boolean indent)
	{
		XMLUtil.serialize(document, stream, indent);
	}
}
