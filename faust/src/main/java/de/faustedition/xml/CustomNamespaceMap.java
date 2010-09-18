package de.faustedition.xml;

import java.net.URI;

import org.juxtasoftware.goddag.io.NamespaceMap;

import de.faustedition.FaustURI;

@SuppressWarnings("serial")
public class CustomNamespaceMap extends NamespaceMap {
    public static final CustomNamespaceMap INSTANCE = new CustomNamespaceMap();
    public static final String TEI_NS_URI = "http://www.tei-c.org/ns/1.0";
    public static final String TEI_SIG_GE_URI = "http://www.tei-c.org/ns/geneticEditions";
    public static final String SVG_NS_URI = "http://www.w3.org/2000/svg";

    public CustomNamespaceMap() {
        super();
        put(URI.create(FaustURI.FAUST_NS_URI), "f");
        put(URI.create(CustomNamespaceMap.TEI_NS_URI), "tei");
        put(URI.create(CustomNamespaceMap.TEI_SIG_GE_URI), "ge");
        put(URI.create(CustomNamespaceMap.SVG_NS_URI), "svg");
        put(URI.create(XMLDatabase.EXIST_NS_URI), "exist");
    }
}
