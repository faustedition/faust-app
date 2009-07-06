package de.faustedition.model.xmldb;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;

public class ExistQueryParameters {
	private String stylesheet;
	private String query = null;
	private boolean indent = false;
	private int resultSize = 0;
	private int resultOffset = 0;
	private boolean wrap = true;
	private boolean createSession = false;
	private String sessionId;

	public static ExistQueryParameters createDefault() {
		return new ExistQueryParameters();
	}

	public String getStylesheet() {
		return stylesheet;
	}

	public void setStylesheet(String stylesheet) {
		this.stylesheet = stylesheet;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public boolean isIndent() {
		return indent;
	}

	public void setIndent(boolean indent) {
		this.indent = indent;
	}

	public int getResultSize() {
		return resultSize;
	}

	public void setResultSize(int resultSize) {
		this.resultSize = resultSize;
	}

	public int getResultOffset() {
		return resultOffset;
	}

	public void setResultOffset(int resultOffset) {
		this.resultOffset = resultOffset;
	}

	public boolean isWrap() {
		return wrap;
	}

	public void setWrap(boolean wrap) {
		this.wrap = wrap;
	}

	public boolean isCreateSession() {
		return createSession;
	}

	public void setCreateSession(boolean createSession) {
		this.createSession = createSession;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public NameValuePair[] toNameValuePairs() {
		List<NameValuePair> nameValuePairs = new LinkedList<NameValuePair>();

		toNameValuePair(nameValuePairs, "_xsl", stylesheet);
		toNameValuePair(nameValuePairs, "_query", query);
		toNameValuePair(nameValuePairs, "_indent", (indent ? null : "no"));
		toNameValuePair(nameValuePairs, "_howmany", (resultSize == 0 ? null : Integer.toString(resultSize)));
		toNameValuePair(nameValuePairs, "_start", (resultOffset == 0 ? null : Integer.toString(resultSize)));
		toNameValuePair(nameValuePairs, "_wrap", (wrap ? null : "no"));
		toNameValuePair(nameValuePairs, "_cache", (createSession ? "yes" : null));
		toNameValuePair(nameValuePairs, "_session", sessionId);

		return nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]);
	}

	private void toNameValuePair(List<NameValuePair> nameValuePairs, String name, String value) {
		if (StringUtils.isNotEmpty(value)) {
			nameValuePairs.add(new NameValuePair(name, value));
		}
	}
}
