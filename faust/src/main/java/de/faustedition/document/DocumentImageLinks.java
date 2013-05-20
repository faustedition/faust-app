package de.faustedition.document;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.faustedition.xml.Namespaces;
import de.faustedition.xml.XPath;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.xpath.XPathExpressionException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Functions for linking XML files with SVG graphics,
 * independent of the Faust-specific backend.
 */

// TODO Consider using SAX for a lower memory footprint

public class DocumentImageLinks {

    public static final String IMAGE_SVG_TYPE = "image/svg+xml";

    /**
     * Get the URI of the link data *
     */
    public static URI readLinkDataURI(org.w3c.dom.Document transcript) {
        final String uri = XPath.toString("/tei:TEI/tei:facsimile/tei:graphic[@mimeType='" + IMAGE_SVG_TYPE + "']/@url", transcript);
        return (Strings.nullToEmpty(uri).trim().isEmpty() ? null : URI.create(uri));
    }

    /**
     * Insert a new URI in the transcript *
     */
    public static void writeLinkDataURI(org.w3c.dom.Document transcript, URI linkData) {
        final Element graphic = transcript.createElementNS(Namespaces.TEI_NS_URI, "graphic");
        graphic.setAttribute("mimeType", IMAGE_SVG_TYPE);
        graphic.setAttribute("url", linkData.toString());

        Node facsimile = Iterables.getFirst(XPath.nodes("//tei:facsimile", transcript), null);
        if (facsimile == null) {
            facsimile = transcript.createElementNS(Namespaces.TEI_NS_URI, "facsimile");
            final Node teiHeader = Iterables.get(XPath.nodes("//tei:teiHeader", transcript), 0);
            teiHeader.getParentNode().insertBefore(teiHeader.getNextSibling(), facsimile);
        }
        facsimile.appendChild(transcript.createComment("Text-image links"));
        facsimile.appendChild(transcript.createTextNode("\n"));
        facsimile.appendChild(graphic);
    }

    /**
     * Output selected elements of the document as a flat JSON list of
     * objects with a temporary id. Changes existing links in {@code svg}
     * to the new temporary id
     *
     * @param transcript The document
     * @param linkData   The SVG document. {@code xlink:href} attributes that point to
     *                   ids in {@code xml} will be changed to the new temporary id. Optional (can be null)
     */
    public static Map<String, Object> read(org.w3c.dom.Document transcript, org.w3c.dom.Document linkData) {
        final IdGenerator lineNumbers = new NumericIdGenerator();
        final Map<String, String> lineIds = new HashMap<String, String>();
        final List<Map<String, Object>> lines = Lists.newLinkedList();

        for (Node lineElement : XPath.nodes("//ge:line", transcript)) {
            final StringBuilder lineContent = new StringBuilder();
            for (Node textNode : XPath.nodes("descendant::text()", lineElement)) {
                lineContent.append(textNode.getTextContent());
            }

            final String lineNumber = lineNumbers.next();
            final String trimmedLineContent = lineContent.toString().trim();
            final Map<String, Object> line = Maps.newHashMap();
            line.put("id", lineNumber);
            line.put("text", trimmedLineContent.length() > 0 ? trimmedLineContent : "(no text)");
            line.put("info", "");
            lines.add(line);

            final Attr lineId = ((Attr) (lineElement.getAttributes().getNamedItemNS(XMLConstants.XML_NS_URI, "id")));
            if (lineId != null) {
                lineIds.put("#" + lineId.getValue(), "#" + lineNumber);
            }
        }

        if (linkData != null) {
            for (Attr link : Iterables.filter(XPath.nodes("//@xlink:href", linkData), Attr.class)) {
                if (lineIds.containsKey(link.getValue())) {
                    link.setValue(lineIds.get(link.getValue()));
                }
            }
        }

        final Map<String, Object> result = Maps.newHashMap();
        result.put("lines", lines);
        return result;
    }

    /**
     * Link an SVG file with an XML file. Any element in {@code svg} can
     * have a an attribute {@code xlink:href} that points to an element in
     * {@code xml}. The elements which can be referenced need not have an
     * {@code xml:id} attribute. Instead, they must be matched by the supplied
     * {@code xp} and are referenced through their position in the matches
     * in document order. <br/><br/>
     * Each referenced element in {@code xml} without an id is then assigned
     * a new id from {@code newIds}, and the links in {@code svg} are adjusted.
     *
     * @param transcript An XML file that is being linked to an SVG file
     * @param linkData   An SVG file with references of the type {@code xlink:href}
     * @return {@code true} if ids have been added to {@code xml}, false otherwise
     * @throws IllegalArgumentException if {@code svg} links to an id outside of
     *                                  {@code tmpIds}
     */
    public static boolean write(org.w3c.dom.Document transcript, org.w3c.dom.Document linkData) {
        final IdGenerator lineNumbers = new NumericIdGenerator();
        final IdGenerator lineIds = new AlphabeticIdGenerator();
        final Iterable<Element> lines = Iterables.filter(XPath.nodes("//ge:line", transcript), Element.class);

        // first pass over elements: note all existing ids
        final Set<String> existingLineIds = new HashSet<String>();
        for (Element line : lines) {
            final Attr id = (Attr) line.getAttributes().getNamedItemNS(XMLConstants.XML_NS_URI, "id");
            if (id != null) {
                existingLineIds.add(id.getValue());
            }
        }

        // Generate and remember the identifier for each element
        final Map<String, Element> enumeratedLines = new HashMap<String, Element>();
        for (Node line : lines) {
            enumeratedLines.put(lineNumbers.next(), (Element) line);
        }

        boolean sourceModified = false;
        for (Attr lineLink : Iterables.filter(XPath.nodes("//@xlink:href", linkData), Attr.class)) {
            final String lineLinkId = lineLink.getValue();
            final Element line = enumeratedLines.get(lineLinkId);
            Preconditions.checkArgument(line != null, String.format("Could not match temporary ID %s. Generator mismatch or too few elements!", lineLinkId));

            final Attr lineId = line.getAttributeNodeNS(XMLConstants.XML_NS_URI, "id");
            if (lineId != null) {
                lineLink.setValue('#' + lineId.getValue());
            } else {
                sourceModified = true;
                String newId = lineIds.next();
                //if the id already exists, try again, but limit the number of attempts
                for (int j = 0, max = enumeratedLines.size(); existingLineIds.contains(newId) && j < max; j++) {
                    newId = lineIds.next();
                }
                line.setAttributeNS(XMLConstants.XML_NS_URI, "id", newId);
                lineLink.setValue('#' + newId);
            }

        }
        return sourceModified;
    }

    /**
     * Must not generate the same id twice in a lifetime
     */
    static abstract class IdGenerator implements Iterator<String> {

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


    static class NumericIdGenerator extends IdGenerator {

        private int index = 0;

        @Override
        public String next() {
            return String.format("lineNumber%d", index++);
        }
    }

    static class AlphabeticIdGenerator extends IdGenerator {
        private int index = 0;

        @Override
        public String next() {
            return String.format("l%s", alphabetify(index++));
        }

        /**
         * Maps a number to a string of lowercase alphabetic characters,
         * which act as digits to base 26. (a, b, c, ..., aa, ab, ac, ...)
         *
         * @param n
         * @return
         */
        private String alphabetify(int n) {
            if (n > 25)
                return alphabetify(n / 26 - 1) + alphabetify(n % 26);
            else
                return "" + (char) (n + 97);

        }
    }
}
