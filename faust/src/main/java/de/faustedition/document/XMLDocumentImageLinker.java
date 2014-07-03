/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.faustedition.document;

import com.google.common.base.Throwables;
import de.faustedition.xml.Namespaces;
import de.faustedition.xml.XPathUtil;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.restlet.data.MediaType;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * Functions for linking XML files with SVG graphics,
 * independent of the Faust-specific backend.
 * 
 */

// TODO Consider using SAX for a lower memory footprint

public class XMLDocumentImageLinker {

	static String XMLNS = "http://www.w3.org/XML/1998/namespace";
	//static String XLINKNS = "http://www.w3.org/1999/xlink";
	static String HREF_XP = "//@xlink:href";
	
	/** Get the URI of the link data **/
	public static URI linkDataURI(org.w3c.dom.Document source) throws XPathExpressionException, URISyntaxException {
		
		//final Node imageLinksElement = imageLinksElement(source);
		
		String query = "/tei:TEI/tei:facsimile/tei:graphic[@mimeType='" + MediaType.IMAGE_SVG.getName() + "']/@url";
		String uri = (String)(XPathUtil.xpath(query).evaluate(source, XPathConstants.STRING));
			
		if ("".equals(uri.trim()) || uri == null)
			return null;
		else
			return new URI(uri);
	}

	
	/** Insert a new URI in the transcript **/
	public static void insertLinkURI (org.w3c.dom.Document xml, URI linkDataURI) throws XPathExpressionException {
			
			final Node teiHeader = xml.getElementsByTagNameNS(Namespaces.TEI_NS_URI, "teiHeader").item(0);
			final NodeList facsimiles = xml.getElementsByTagNameNS(Namespaces.TEI_NS_URI, "facsimile");
			Node facsimile;
			if (facsimiles.getLength() > 0)
				facsimile = facsimiles.item(0);			
			else { 
				facsimile = xml.createElementNS(Namespaces.TEI_NS_URI, "facsimile");
				teiHeader.getParentNode().insertBefore(teiHeader.getNextSibling(), facsimile);
			}
//			Node lastGraphic = (Node)(XPathUtil.xpath("/tei:TEI/tei:facsimile/tei:graphic[last()]")
//					.evaluate(xml, XPathConstants.NODE));
			
			final Node graphic = xml.createElementNS(Namespaces.TEI_NS_URI, "graphic");
			
			Attr mimeType = xml.createAttribute("mimeType");
			mimeType.setNodeValue(MediaType.IMAGE_SVG.getName());
			graphic.getAttributes().setNamedItem(mimeType);
			
			Attr url = xml.createAttribute("url");
			url.setNodeValue(linkDataURI.toString());
			graphic.getAttributes().setNamedItem(url);
			
			facsimile.appendChild(xml.createComment("Text-image links"));
			facsimile.appendChild(xml.createTextNode("\n"));
			facsimile.appendChild(graphic);
			
		}
	
	/**
	 * Output selected elements of the document as a flat JSON list of
	 * objects with a temporary id. Changes existing links in {@code svg}
	 * to the new temporary id
	 * @param xml The document
	 * @param svg The SVG document. {@code xlink:href} attributes that point to 
	 * ids in {@code xml} will be changed to the new temporary id. Optional (can be null)
	 * @param xp An XPath expression whose evaluation yields the target elements 
	 * @param tmpIds Generates ids to identify the elements yielded by {@code xp}
	 * @param outputStream The stream to write the result to
	 * @throws IOException
	 */
	public static void enumerateTarget(org.w3c.dom.Document xml, org.w3c.dom.Document svg, XPathExpression xp,
			IdGenerator tmpIds, OutputStream outputStream) throws IOException {

		final JsonGenerator generator = new JsonFactory().createJsonGenerator(outputStream, JsonEncoding.UTF8);
		int lineCount = 0;
		
		try {

			Map<String, String> originalIdMap = new HashMap<String, String>();
			
			NodeList elements= (NodeList) (xp.evaluate(xml,
					XPathConstants.NODESET));

			generator.writeStartObject();
			generator.writeArrayFieldStart("lines");

			for (int i = 0; i < elements.getLength(); i++) {
				Node element = elements.item(i);
				
				
				String textQuery = "descendant::text()";
				
				NodeList text = (NodeList) (XPathUtil.xpath(textQuery)
						.evaluate(element, XPathConstants.NODESET));
				StringBuilder lineContent = new StringBuilder();
				for (int j=0; j < text.getLength(); j++)
					lineContent.append(text.item(j).getTextContent());

				String tmpId = tmpIds.next();
				Attr origId = ((Attr)(element.getAttributes().getNamedItemNS(XMLNS, "id")));
				if (origId != null)
					originalIdMap.put("#" + origId.getValue(), "#" + tmpId);
				try {
					generator.writeStartObject();
					generator.writeStringField("id", tmpId);
					String trimmedLineContent = lineContent.toString().trim();
					generator.writeStringField("text", trimmedLineContent.length() > 0? trimmedLineContent: "(no text)");
					generator.writeStringField("info", "");
					generator.writeEndObject();
				} catch (IOException e) {
					throw Throwables.propagate(e);
				}

				lineCount++;
			}
			
			generator.writeEndArray();
			generator.writeEndObject();
			generator.close();
			

			XPathExpression hrefXP = XPathUtil.xpath(HREF_XP);			
			
			if (svg != null) {
				NodeList links = (NodeList) (hrefXP
						.evaluate(svg, XPathConstants.NODESET));
				for (int i  = 0; i < links.getLength(); i++) {
					Attr link = (Attr)(links.item(i));
					if (originalIdMap.containsKey(link.getValue()))
						link.setValue(originalIdMap.get(link.getValue()));
				}
			}

		} catch (XPathExpressionException e1) {
			throw Throwables.propagate(e1);
		}

	}

	/**
	 * Must not generate the same id twice in a lifetime
	 */

	public static abstract class IdGenerator implements Iterator<String>{

		@Override
		public boolean hasNext() {return true;}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * Link an SVG file with an XML file. Any element in {@code svg} can
	 * have a an attribute {@code xlink:href} that points to an element in 
	 * {@code xml}. The elements which can be referenced need not have an
	 * {@code xml:id} attribute. Instead, they must be matched by the supplied
	 * {@code xp} and are referenced through their position in the matches 
	 * in document order. <br/><br/>
	 * Each referenced element in {@code xml} without an id is then assigned
	 *  a new id from {@code newIds}, and the links in {@code svg} are adjusted.
	 * 
	 * @param xml An XML file that is being linked to an SVG file
	 * @param tmpIds Generates sequential ids that are referenced from {@code svg}.
	 * The n-th match of {@code xp} in {@code xml} is identified by the n-th
	 * element of {@code tmpIds}
	 * @param xp Matches the elements in the XML file to be targets of links.
	 * Only elements may be matched!
	 * @param svg An SVG file with references of the type {@code xlink:href}
	 *  with values from {@code tmpIds}
	 * @param newIds New Ids for the elements in {@code xml} that do not have
	 * an id yet
	 * @return {@code true} if ids have been added to {@code xml}, false otherwise
 	 * @throws IllegalArgumentException if {@code svg} links to an id outside of
	 * {@code tmpIds} 
	 */


	public static boolean link(org.w3c.dom.Document xml, IdGenerator tmpIds,
			XPathExpression xp, org.w3c.dom.Document svg, IdGenerator newIds) {

		boolean idsAdded = false;
		
		final NodeList elements;
		try {
			elements = (NodeList) (xp
					.evaluate(xml, XPathConstants.NODESET));
			
			for (int i = 0; i < elements.getLength(); i++)
				if (elements.item(i).getNodeType() != Node.ELEMENT_NODE)
					throw new IllegalArgumentException("XPath may only match elements!");
			
			final HashSet<String> ids = new HashSet<String>();
			
			// first pass over elements: note all existing ids
			for (int i = 0; i < elements.getLength(); i++) {
				 Attr id = (Attr)
				 	(elements.item(i).getAttributes().getNamedItemNS(XMLNS, "id"));
				if (id!=null)
					ids.add(id.getValue());
			}

			
			XPathExpression hrefXP = XPathUtil.xpath(HREF_XP);			

			NodeList links = (NodeList) (hrefXP
					.evaluate(svg, XPathConstants.NODESET));
			
			HashMap<String, Element> tmpIdMap = new HashMap<String, Element>();			
			
			// Generate and remember the identifier for each element
			for (int i = 0; i < elements.getLength(); i++) 
				tmpIdMap.put(tmpIds.next(), (Element)(elements.item(i)));
			
			for (int i = 0; i < links.getLength(); i++) {
				Attr link = (Attr) (links.item(i));
				String val = link.getValue();
				if (!tmpIdMap.containsKey(val))
					throw new IllegalArgumentException(
							String.format("Could not match temporary ID %s. Generator mismatch or too few elements!", val));
				Element element = tmpIdMap.get(val);
				Attr existingId = element.getAttributeNodeNS(XMLNS, "id");
				if (existingId != null)
					link.setValue('#' + existingId.getValue());
				else {
					idsAdded = true;
					String newId = newIds.next();
					//if the id already exists, try again, but limit the number of attempts
					for (int j = 0; ids.contains(newId) && j < elements.getLength(); j++)
						newId = newIds.next();
						
					element.setAttributeNS(XMLNS, "id", newId);
					link.setValue('#' + newId);
				}
				
			}
						
		} catch (XPathExpressionException e) {
			Throwables.propagate(e);
		}
		return idsAdded;
	}

}
