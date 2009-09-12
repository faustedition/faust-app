package de.faustedition.web.dav;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import nu.xom.Attribute;
import nu.xom.DocType;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

public abstract class CollectionDavResourceBase extends DavResourceBase implements CollectionResource, GetableResource {

	private static final String XHTML_NS_URI = "http://www.w3.org/1999/xhtml";
	private static final String XHTML_PUB_ID = "-//W3C//DTD XHTML 1.0 Strict//EN";
	private static final String XHTML_SYSTEM_ID = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd";

	protected CollectionDavResourceBase(DavResourceFactory factory) {
		super(factory);
	}

	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException {
		Element childListElement = new Element("ul", XHTML_NS_URI);

		for (Resource child : getChildren()) {
			String name = child.getName() + (child instanceof CollectionResource ? "/" : "");
			Element linkElement = new Element("a", XHTML_NS_URI);
			linkElement.addAttribute(new Attribute("href", name));
			linkElement.addAttribute(new Attribute("title", name));
			linkElement.appendChild(name);

			Element itemElement = new Element("li", XHTML_NS_URI);
			itemElement.appendChild(linkElement);
			childListElement.appendChild(itemElement);
		}

		Element bodyElement = new Element("body", XHTML_NS_URI);
		bodyElement.appendChild(childListElement);

		Element titleElement = new Element("title", XHTML_NS_URI);
		titleElement.appendChild(getName());
		Element headElement = new Element("head", XHTML_NS_URI);
		headElement.appendChild(titleElement);

		Element htmlElement = new Element("html", XHTML_NS_URI);
		htmlElement.appendChild(headElement);
		htmlElement.appendChild(bodyElement);

		Document document = new Document(htmlElement);
		document.setDocType(new DocType("html", XHTML_PUB_ID, XHTML_SYSTEM_ID));

		Serializer serializer = new Serializer(out, "UTF-8");
		serializer.setIndent(4);
		serializer.setMaxLength(0);
		serializer.write(document);
	}

	@Override
	public String getContentType(String accepts) {
		return "application/xhtml+xml";
	}

	@Override
	public Long getMaxAgeSeconds(Auth auth) {
		return null;
	}

	@Override
	public Long getContentLength() {
		return null;
	}

	@Override
	public String checkRedirect(Request request) {
		return (!request.getAbsolutePath().endsWith("/") ? (request.getAbsoluteUrl() + "/") : super.checkRedirect(request));
	}
}
