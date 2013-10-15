package de.faustedition.xml;

import com.google.common.base.Preconditions;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Namespaces implements NamespaceContext {

    public static final String FAUST_NS_URI = "http://www.faustedition.net/ns";
    public static final String FAUST_NS_PREFIX = "f";
    public static final String TEI_NS_URI = "http://www.tei-c.org/ns/1.0";
    public static final String TEI_NS_PREFIX = "tei";
    public static final String TEI_SIG_GE_URI = "http://www.tei-c.org/ns/geneticEditions";
    public static final URI TEI_SIG_GE = URI.create(TEI_SIG_GE_URI);
    public static final String TEI_SIG_GE_PREFIX = "ge";
    public static final String SVG_NS_URI = "http://www.w3.org/2000/svg";
    public static final String SVG_NS_PREFIX = "svg";
    public static final String XLINK_NS_URI = "http://www.w3.org/1999/xlink";
    public static final String XLINK_NS_PREFIX = "xlink";

    private Map<String, String> prefixToNamespaceUri = new HashMap<String, String>();
    private Map<String, List<String>> namespaceUriToPrefixes = new HashMap<String, List<String>>();

    private String defaultNamespaceUri = "";

    public String getNamespaceURI(String prefix) {
        Preconditions.checkArgument(prefix != null, "prefix is null");
        if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
            return XMLConstants.XML_NS_URI;
        } else if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        } else if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
            return defaultNamespaceUri;
        } else if (prefixToNamespaceUri.containsKey(prefix)) {
            return prefixToNamespaceUri.get(prefix);
        }
        return "";
    }

    public String getPrefix(String namespaceUri) {
        List<?> prefixes = getPrefixesInternal(namespaceUri);
        return prefixes.isEmpty() ? null : (String) prefixes.get(0);
    }

    public Iterator<?> getPrefixes(String namespaceUri) {
        return getPrefixesInternal(namespaceUri).iterator();
    }

    public void bindNamespaceUri(String prefix, String namespaceUri) {
        Preconditions.checkArgument(prefix != null, "No prefix given");
        Preconditions.checkArgument(namespaceUri != null, "No namespaceUri given");
        if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
            defaultNamespaceUri = namespaceUri;
        } else {
            prefixToNamespaceUri.put(prefix, namespaceUri);
            getPrefixesInternal(namespaceUri).add(prefix);
        }
    }

    public void clear() {
        prefixToNamespaceUri.clear();
    }

    private List<String> getPrefixesInternal(String namespaceUri) {
        if (defaultNamespaceUri.equals(namespaceUri)) {
            return Collections.singletonList(XMLConstants.DEFAULT_NS_PREFIX);
        } else if (XMLConstants.XML_NS_URI.equals(namespaceUri)) {
            return Collections.singletonList(XMLConstants.XML_NS_PREFIX);
        } else if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceUri)) {
            return Collections.singletonList(XMLConstants.XMLNS_ATTRIBUTE);
        } else {
            List<String> list = namespaceUriToPrefixes.get(namespaceUri);
            if (list == null) {
                list = new ArrayList<String>();
                namespaceUriToPrefixes.put(namespaceUri, list);
            }
            return list;
        }
    }

    public static final Namespaces INSTANCE = new Namespaces();

    static {
        INSTANCE.bindNamespaceUri(FAUST_NS_PREFIX, FAUST_NS_URI);
        INSTANCE.bindNamespaceUri(TEI_NS_PREFIX, TEI_NS_URI);
        INSTANCE.bindNamespaceUri(TEI_SIG_GE_PREFIX, TEI_SIG_GE_URI);
        INSTANCE.bindNamespaceUri(SVG_NS_PREFIX, SVG_NS_URI);
        INSTANCE.bindNamespaceUri(XLINK_NS_PREFIX, XLINK_NS_URI);
    }
}
