package de.faustedition.text;

import static de.faustedition.xml.CustomNamespaceMap.TEI_NS_URI;

import java.io.IOException;
import java.util.Stack;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.base.Joiner;
import com.google.inject.Inject;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.Runtime;
import de.faustedition.xml.NodeListWrapper;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;
import de.faustedition.xml.XPathUtil;

public class TextSplitter extends Runtime implements Runnable {

	private final XMLStorage xml;

	@Inject
	public TextSplitter(XMLStorage xml) {
		this.xml = xml;
	}

	@Override
	public void run() {
		try {
			XPathExpression bodyXP = XPathUtil.xpath("//tei:body");
			for (FaustURI text : xml.iterate(new FaustURI(FaustAuthority.XML, "/text"))) {
				final String uriPath = text.getPath();
				final int prefix = Integer.parseInt(uriPath.substring(uriPath.lastIndexOf("_") + 1, uriPath.length() - 4));
				
				final Document document = XMLUtil.parse(xml.getInputSource(text));
				final Element body = new NodeListWrapper<Element>(bodyXP, document).singleResult(Element.class);
				final Stack<Integer> path = new Stack<Integer>();
				path.push(prefix);
				exportDivs(path, body);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void exportDivs(Stack<Integer> path, Element context) throws XPathExpressionException, IOException, TransformerException {
		int divCount = 0;
		if (path.size() < 3) {
			for (Element div : new NodeListWrapper<Element>(XPathUtil.xpath("./tei:div"), context)) {
				path.push(++divCount);
				exportDivs(path, div);
				path.pop();
			}
		}
		if (divCount == 0) {
			System.out.println(Joiner.on(" -> ").join(path));
			final Document baseDocument = context.getOwnerDocument();
			final Document document = XMLUtil.documentBuilder().newDocument();
			for (Node rootNode : new NodeListWrapper<Node>(baseDocument.getChildNodes())) {
				document.appendChild(document.adoptNode(rootNode.cloneNode(false)));
			}
			final XPathExpression headerXP = XPathUtil.xpath("//tei:teiHeader");
			final Element header = new NodeListWrapper<Element>(headerXP, baseDocument).singleResult(Element.class);
			final Element root = document.getDocumentElement();
			root.appendChild(document.adoptNode(header.cloneNode(true)));
			
			final Element text = document.createElementNS(TEI_NS_URI, "text");
			root.appendChild(text);
			
			final Element body = document.createElementNS(TEI_NS_URI, "body");
			text.appendChild(body);
			
			body.appendChild(document.adoptNode(context.cloneNode(true)));
			
			xml.put(new FaustURI(FaustAuthority.XML, "/text/" + Joiner.on('-').join(path) + ".xml"), document);
			return;
		}
	}
	
	public static void main(String[] args) {
		try {
			main(TextSplitter.class, args);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

}
