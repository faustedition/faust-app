package de.faustedition.model.xmldb;

import static de.faustedition.model.xmldb.XmlDbManager.EXIST_NS_URI;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlDbQueryResult {

	private final Element root;
	private int hits;
	private int start;
	private int count;
	private String sessionId;

	public static XmlDbQueryResult parse(Document document) {
		Element resultElement = document.getDocumentElement();
		if (!"result".equals(resultElement.getLocalName()) || !EXIST_NS_URI.equals(resultElement.getNamespaceURI())) {
			throw new IllegalArgumentException();
		}
		XmlDbQueryResult result = new XmlDbQueryResult(resultElement);
		if (resultElement.hasAttribute("hits")) {
			result.hits = Integer.parseInt(resultElement.getAttribute("hits"));
		}
		if (resultElement.hasAttribute("start")) {
			result.start = Integer.parseInt(resultElement.getAttribute("start"));
		}
		if (resultElement.hasAttribute("count")) {
			result.count = Integer.parseInt(resultElement.getAttribute("count"));
		}
		if (resultElement.hasAttribute("session")) {
			result.sessionId = resultElement.getAttribute("session");
		}

		return result;
	}

	public Element getRoot() {
		return root;
	}

	public int getHits() {
		return hits;
	}

	public int getStart() {
		return start;
	}

	public int getCount() {
		return count;
	}

	public String getSessionId() {
		return sessionId;
	}

	private XmlDbQueryResult(Element root) {
		this.root = root;
	}

}
