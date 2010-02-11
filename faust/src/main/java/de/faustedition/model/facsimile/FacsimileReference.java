package de.faustedition.model.facsimile;

import static de.faustedition.model.tei.EncodedTextDocument.TEI_NS_URI;
import static de.faustedition.model.tei.EncodedTextDocument.xpath;
import static de.faustedition.model.xmldb.NodeListIterable.singleResult;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.faustedition.model.tei.EncodedTextDocument;
import de.faustedition.model.xmldb.NodeListIterable;
import de.faustedition.util.URIUtil;
import de.faustedition.util.XMLUtil;

public class FacsimileReference {
	private static final String FACSIMILE_AUTHORITY = "facsimile";

	private String path;

	public FacsimileReference(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public static FacsimileReference fromURI(URI uri) throws URISyntaxException {
		if (!FACSIMILE_AUTHORITY.equals(uri.getAuthority())) {
			throw new URISyntaxException(uri.toASCIIString(), "Not in facsimile authority");
		}
		return new FacsimileReference(StringUtils.strip(uri.getPath(), "/"));
	}

	public URI toURI() {
		return URIUtil.create(FACSIMILE_AUTHORITY, "/" + path);
	}

	public static List<FacsimileReference> readFrom(EncodedTextDocument document) throws URISyntaxException {
		List<FacsimileReference> references = new ArrayList<FacsimileReference>();
		for (Element graphicEl : new NodeListIterable<Element>(xpath("//tei:facsimile/tei:graphic"), document)) {
			if (!graphicEl.hasAttributeNS(TEI_NS_URI, "url")) {
				continue;
			}

			references.add(fromURI(URIUtil.parse(graphicEl.getAttributeNS(TEI_NS_URI, "url"))));

		}
		return references;
	}

	public static void writeTo(EncodedTextDocument document, List<FacsimileReference> references) {
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
			XMLUtil.removeChildren(facsimile);
		}

		for (FacsimileReference reference : references) {
			Element graphicEl = d.createElementNS(TEI_NS_URI, "graphic");
			graphicEl.setAttributeNS(TEI_NS_URI, "url", reference.toURI().toASCIIString());
			facsimile.appendChild(graphicEl);
		}
	}
}
