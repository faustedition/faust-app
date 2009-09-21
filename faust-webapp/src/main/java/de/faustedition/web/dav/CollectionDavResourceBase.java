package de.faustedition.web.dav;

import static net.sf.practicalxml.builder.XmlBuilder.attribute;
import static net.sf.practicalxml.builder.XmlBuilder.element;
import static net.sf.practicalxml.builder.XmlBuilder.text;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.practicalxml.builder.ElementNode;
import net.sf.practicalxml.builder.Node;

import org.w3c.dom.Document;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

import de.faustedition.util.ErrorUtil;
import de.faustedition.util.XMLUtil;

public abstract class CollectionDavResourceBase extends DavResourceBase implements CollectionResource, GetableResource
{

	private static final String XHTML_NS_URI = "http://www.w3.org/1999/xhtml";
	private static final String XHTML_PUB_ID = "-//W3C//DTD XHTML 1.0 Strict//EN";
	private static final String XHTML_SYSTEM_ID = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd";

	protected CollectionDavResourceBase(DavResourceFactory factory)
	{
		super(factory);
	}

	private static ElementNode htmlElement(String localName, Node... children)
	{
		return element(XHTML_NS_URI, localName, children);
	}

	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException
	{
		List<? extends Resource> children = getChildren();
		List<Node> entryLinks = new ArrayList<Node>(children.size());
		for (Resource child : children)
		{
			String name = child.getName() + (child instanceof CollectionDavResourceBase ? "/" : "");
			entryLinks.add(htmlElement("li", htmlElement("a", attribute("href", name), attribute("title", name), text(name))));
		}

		ElementNode bodyElement = htmlElement("body", htmlElement("ul", entryLinks.toArray(new Node[entryLinks.size()])));
		Document document = htmlElement("html", htmlElement("head", htmlElement("title", text(getName()))), bodyElement).toDOM();

		try
		{
			Transformer transformer = XMLUtil.nullTransformer(false);
			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, XHTML_PUB_ID);
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, XHTML_SYSTEM_ID);
			transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
			transformer.transform(new DOMSource(document), new StreamResult(out));
		} catch (TransformerException e)
		{
			throw ErrorUtil.fatal("Error serializing XHTML view", e);
		}
	}

	@Override
	public String getContentType(String accepts)
	{
		return "application/xhtml+xml";
	}

	@Override
	public Long getMaxAgeSeconds(Auth auth)
	{
		return null;
	}

	@Override
	public Long getContentLength()
	{
		return null;
	}

	@Override
	public String checkRedirect(Request request)
	{
		return (!request.getAbsolutePath().endsWith("/") ? (request.getAbsoluteUrl() + "/") : super.checkRedirect(request));
	}
}
