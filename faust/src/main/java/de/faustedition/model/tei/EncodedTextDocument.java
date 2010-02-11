package de.faustedition.model.tei;

import static de.faustedition.model.xml.NodeListIterable.singleResult;

import java.io.InputStream;

import javax.xml.xpath.XPathExpression;

import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Preconditions;

import de.faustedition.model.xml.XPathUtil;
import de.faustedition.model.xml.XmlDocument;
import de.faustedition.model.xml.XmlUtil;

public class EncodedTextDocument extends XmlDocument {
	public static final String TEI_NS_URI = "http://www.tei-c.org/ns/1.0";
	public static final String TEI_SIG_GE_URI = "http://www.tei-c.org/ns/geneticEditions";
	public static final String SVG_NS_URI = "http://www.w3.org/2000/svg";
	private static final SimpleNamespaceContext NS_CONTEXT = new SimpleNamespaceContext();

	static {
		NS_CONTEXT.bindNamespaceUri("tei", TEI_NS_URI);
		NS_CONTEXT.bindNamespaceUri("svg", SVG_NS_URI);
		NS_CONTEXT.bindNamespaceUri("ge", TEI_SIG_GE_URI);
	}

	public EncodedTextDocument(Document document) {
		super(document);
		Element root = document.getDocumentElement();
		String localName = root.getLocalName();
		if (!TEI_NS_URI.equals(root.getNamespaceURI()) || (!"TEI".equals(localName) && !"teiCorpus".equals(localName))) {
			throw new EncodedTextDocumentException("Provided DOM is not a TEI document");
		}
	}

	public static EncodedTextDocument create() {
		Document document = new XmlDocument().getDom();
		document.appendChild(document.createElementNS(TEI_NS_URI, "TEI"));
		return new EncodedTextDocument(document);
	}

	public static EncodedTextDocument parse(InputStream inputStream) {
		return new EncodedTextDocument(XmlUtil.parse(inputStream));
	}

	public Element getTextElement() {
		return Preconditions.checkNotNull(singleResult(xpath("//tei:text"), dom, Element.class));
	}

	public Element getRevisionElement() {
		return Preconditions.checkNotNull(singleResult(xpath("//tei:revisionDesc"), dom, Element.class));
	}

	public static XPathExpression xpath(String expr) {
		return XPathUtil.xpath(expr, NS_CONTEXT);
	}
}
