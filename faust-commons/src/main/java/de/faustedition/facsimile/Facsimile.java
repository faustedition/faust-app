package de.faustedition.facsimile;

import static de.faustedition.tei.EncodedTextDocument.TEI_NS_URI;
import static de.faustedition.tei.EncodedTextDocument.xpath;
import static de.faustedition.xml.NodeListIterable.singleResult;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.faustedition.URIUtil;
import de.faustedition.tei.EncodedTextDocument;
import de.faustedition.xml.NodeListIterable;
import de.faustedition.xml.XmlUtil;

public class Facsimile implements Comparable<Facsimile> {
	private static final String FACSIMILE_AUTHORITY = "facsimile";

	private String path;

	public Facsimile(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public static Facsimile fromUri(URI uri) throws URISyntaxException {
		if (!FACSIMILE_AUTHORITY.equals(uri.getAuthority())) {
			throw new URISyntaxException(uri.toASCIIString(), "Not in facsimile authority");
		}
		return new Facsimile(StringUtils.strip(uri.getPath(), "/"));
	}

	public URI toUri() {
		return URIUtil.create(FACSIMILE_AUTHORITY, "/" + path);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Facsimile) {
			return path.equals(((Facsimile) obj).path);
		}

		return super.equals(obj);
	}

	@Override
	public int compareTo(Facsimile o) {
		return path.compareTo(o.path);
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}

	@Override
	public String toString() {
		return toUri().toString();
	}
	
	public static List<Facsimile> readFrom(EncodedTextDocument document) throws URISyntaxException {
		List<Facsimile> references = new ArrayList<Facsimile>();
		for (Element graphicEl : new NodeListIterable<Element>(xpath("//tei:facsimile/tei:graphic"), document)) {
			if (!graphicEl.hasAttributeNS(TEI_NS_URI, "url")) {
				continue;
			}

			references.add(fromUri(URIUtil.parse(graphicEl.getAttributeNS(TEI_NS_URI, "url"))));

		}
		return references;
	}

	public static void writeTo(EncodedTextDocument document, List<Facsimile> references) {
		Document d = document.getDom();
		Element facsimile = singleResult(xpath("//tei:facsimile"), d, Element.class);
		if (facsimile == null) {
			facsimile = d.createElementNS(TEI_NS_URI, "facsimile");
			Element documentEl = d.getDocumentElement();
			Element headerEl = singleResult(xpath("//tei:teiHeader"), d, Element.class);
			if (headerEl == null) {
				documentEl.insertBefore(facsimile, documentEl.getFirstChild());
			} else {
				documentEl.insertBefore(facsimile, headerEl.getNextSibling());
			}
		} else {
			XmlUtil.removeChildren(facsimile);
		}

		for (Facsimile reference : references) {
			Element graphicEl = d.createElementNS(TEI_NS_URI, "graphic");
			graphicEl.setAttributeNS(TEI_NS_URI, "url", reference.toUri().toASCIIString());
			facsimile.appendChild(graphicEl);
		}
	}
}
