package de.faustedition.model.dav;

import static net.sf.practicalxml.builder.XmlBuilder.attribute;
import static net.sf.practicalxml.builder.XmlBuilder.element;
import static net.sf.practicalxml.builder.XmlBuilder.text;

import java.io.IOException;
import java.io.InputStream;
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

import org.hibernate.classic.Session;
import org.w3c.dom.Document;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

import de.faustedition.model.hierarchy.HierarchyNode;
import de.faustedition.model.hierarchy.HierarchyNodeFacet;
import de.faustedition.model.hierarchy.HierarchyNodeType;
import de.faustedition.util.ErrorUtil;
import de.faustedition.util.XMLUtil;

public class HierarchyNodeDavResource extends DavResource implements FolderResource {
	private static final String XHTML_NS_URI = "http://www.w3.org/1999/xhtml";
	private static final String XHTML_PUB_ID = "-//W3C//DTD XHTML 1.0 Strict//EN";
	private static final String XHTML_SYSTEM_ID = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd";

	private final HierarchyNode node;

	protected HierarchyNodeDavResource(DavResourceFactory factory, HierarchyNode node) {
		super(factory);
		this.node = node;
	}

	@Override
	public String getName() {
		return node.getName();
	}

	private static ElementNode htmlElement(String localName, Node... children) {
		return element(XHTML_NS_URI, localName, children);
	}

	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException,
			NotAuthorizedException {
		List<? extends Resource> children = getChildren();
		List<Node> entryLinks = new ArrayList<Node>(children.size());
		for (Resource child : children) {
			String name = child.getName() + (child instanceof HierarchyNodeDavResource ? "/" : "");
			entryLinks.add(htmlElement("li", htmlElement("a", attribute("href", name), attribute("title", name),
					text(name))));
		}

		ElementNode bodyElement = htmlElement("body", htmlElement("ul", entryLinks.toArray(new Node[entryLinks.size()])));
		Document document = htmlElement("html", htmlElement("head", htmlElement("title", text(getName()))), bodyElement)
				.toDOM();

		try {
			Transformer transformer = XMLUtil.nullTransformer(false);
			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, XHTML_PUB_ID);
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, XHTML_SYSTEM_ID);
			transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
			transformer.transform(new DOMSource(document), new StreamResult(out));
		} catch (TransformerException e) {
			throw ErrorUtil.fatal(e, "Error serializing XHTML view");
		}
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

	@Override
	public Resource child(String childName) {
		HierarchyNode child = node.findChild(factory.getSessionFactory().getCurrentSession(), childName);
		return (child == null ? factory.createResource(node, childName) : factory.createResource(child, null));
	}

	@Override
	public List<? extends Resource> getChildren() {
		Session session = factory.getSessionFactory().getCurrentSession();

		List<HierarchyNode> childNodes = node.findChildren(session);
		List<Resource> children = new ArrayList<Resource>(childNodes.size());
		for (HierarchyNode childNode : childNodes) {
			children.add(factory.createResource(childNode));
		}

		for (HierarchyNodeFacet facet : HierarchyNodeFacet.findByNode(session, node).values()) {
			Resource resource = factory.createResource(facet);
			if (resource != null) {
				children.add(resource);
			}
		}

		return children;
	}

	@Override
	public Object getLockResource() {
		return node;
	}

	@Override
	public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException {
		Session session = factory.getSessionFactory().getCurrentSession();
		HierarchyNode child = node.findChild(session, newName);
		if (child != null) {
			throw new ConflictException(factory.createResource(child));
		}
		child = new HierarchyNode(node, newName, HierarchyNodeType.FILE).save(session);
		return factory.createResource(child);
	}

	@Override
	public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void copyTo(CollectionResource toCollection, String name) {
		// TODO Auto-generated method stub
	}

	@Override
	public void delete() {
		node.delete(factory.getSessionFactory().getCurrentSession());
	}

	@Override
	public void moveTo(CollectionResource rDest, String name) throws ConflictException {
		// TODO Auto-generated method stub

	}
}
