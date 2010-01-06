package de.faustedition.model.tei;

import static de.faustedition.model.tei.EncodedDocument.TEI_NS_URI;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.faustedition.util.XMLUtil;

public class HeaderTemplateProcessor implements EncodedDocumentProcessor
{

	private static final String DEFAULT_TITLE = "Johann Wolfgang von Goethe: Faust";

	@Override
	public void process(EncodedDocument teiDocument)
	{
		Document domDocument = teiDocument.getDocument();

		Element teiHeaderElement = teiDocument.findElementByPath("teiHeader");
		if (teiHeaderElement == null)
		{
			Element documentElement = domDocument.getDocumentElement();
			documentElement.insertBefore(teiHeaderElement = domDocument.createElementNS(TEI_NS_URI, "teiHeader"), documentElement.getFirstChild());
		}

		Element profileDescElement = XMLUtil.getChild(teiHeaderElement, "profileDesc");
		if (profileDescElement == null)
		{
			teiHeaderElement.appendChild(profileDescElement = domDocument.createElementNS(TEI_NS_URI, "profileDesc"));
			profileDescElement.appendChild(domDocument.createElementNS(TEI_NS_URI, "p"));
		}

		Element encodingDescElement = XMLUtil.getChild(teiHeaderElement, "encodingDesc");
		if (encodingDescElement == null)
		{
			teiHeaderElement.insertBefore(encodingDescElement = domDocument.createElementNS(TEI_NS_URI, "encodingDesc"), profileDescElement);
			encodingDescElement.appendChild(domDocument.createElementNS(TEI_NS_URI, "p"));
		}

		Element fileDescElement = XMLUtil.getChild(teiHeaderElement, "fileDesc");
		if (fileDescElement == null)
		{
			teiHeaderElement.insertBefore(fileDescElement = domDocument.createElementNS(TEI_NS_URI, "fileDesc"), encodingDescElement);
		}

		Element sourceDescElement = XMLUtil.getChild(fileDescElement, "sourceDesc");
		if (sourceDescElement == null)
		{
			fileDescElement.appendChild(sourceDescElement = domDocument.createElementNS(TEI_NS_URI, "sourceDesc"));
			sourceDescElement.appendChild(domDocument.createElementNS(TEI_NS_URI, "p"));
		}

		Element publicationStmtElement = XMLUtil.getChild(fileDescElement, "publicationStmt");
		if (publicationStmtElement == null)
		{
			fileDescElement.insertBefore(publicationStmtElement = domDocument.createElementNS(TEI_NS_URI, "publicationStmt"), sourceDescElement);
			publicationStmtElement.appendChild(domDocument.createElementNS(TEI_NS_URI, "p"));
		}

		Element titleStmtElement = XMLUtil.getChild(fileDescElement, "titleStmt");
		if (titleStmtElement == null)
		{
			fileDescElement.insertBefore(titleStmtElement = domDocument.createElementNS(TEI_NS_URI, "titleStmt"), fileDescElement.getFirstChild());
		}

		Element titleElement = XMLUtil.getChild(titleStmtElement, "title");
		if (titleElement == null)
		{
			titleStmtElement.insertBefore(titleElement = domDocument.createElementNS(TEI_NS_URI, "title"), titleStmtElement.getFirstChild());
			titleElement.setTextContent(DEFAULT_TITLE);
		}

	}
}
