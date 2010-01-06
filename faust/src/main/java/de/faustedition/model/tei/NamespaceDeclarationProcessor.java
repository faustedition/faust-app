package de.faustedition.model.tei;

import javax.xml.XMLConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NamespaceDeclarationProcessor implements EncodedDocumentProcessor
{

	@Override
	public void process(EncodedDocument teiDocument)
	{
		Document domDocument = teiDocument.getDocument();
		Element documentElement = domDocument.getDocumentElement();

		if (!documentElement.hasAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "svg"))
		{
			documentElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:svg", EncodedDocument.SVG_NS_URI);
		}

		if (!documentElement.hasAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "ge"))
		{
			documentElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:ge", EncodedDocument.TEI_SIG_GE_URI);
		}
	}

}
