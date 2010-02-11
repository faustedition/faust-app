package de.faustedition.model.xmldb;

import static de.faustedition.model.xmldb.XmlDbManager.EXIST_NS_URI;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.faustedition.util.XMLUtil;

public class XmlDbQuery {
	private int start = 0;
	private int max = 0;
	private boolean cache = false;
	private String sessionId;
	private String xquery;

	public XmlDbQuery(String xquery) {
		this.xquery = xquery;
	}
	
	public Document toDocument() {
		Document d = XMLUtil.documentBuilder().newDocument();

		Element textEl = d.createElementNS(EXIST_NS_URI, "text");
		textEl.setTextContent(xquery);

		Element queryEl = d.createElementNS(EXIST_NS_URI, "query");
		queryEl.appendChild(textEl);
		if (start > 0) {
			queryEl.setAttribute("start", Integer.toString(start));
		}
		if (max > 0) {
			queryEl.setAttribute("max", Integer.toString(max));
		}
		if (cache) {
			queryEl.setAttribute("cache", "yes");
		}
		if (sessionId != null) {
			queryEl.setAttribute("session-id", sessionId);
		}

		d.appendChild(queryEl);
		return d;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public boolean isCache() {
		return cache;
	}

	public void setCache(boolean cache) {
		this.cache = cache;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getXquery() {
		return xquery;
	}

	public void setXquery(String xquery) {
		this.xquery = xquery;
	}

}
