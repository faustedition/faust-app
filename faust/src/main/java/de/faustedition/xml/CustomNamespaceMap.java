package de.faustedition.xml;

import java.net.URI;

import org.goddag4j.io.NamespaceMap;

@SuppressWarnings("serial")
public class CustomNamespaceMap extends NamespaceMap {
	public static final CustomNamespaceMap INSTANCE = new CustomNamespaceMap();

	public static final String FAUST_NS_URI = "http://www.faustedition.net/ns";
	public static final String FAUST_NS_PREFIX = "f";

	public static final String TEI_NS_URI = "http://www.tei-c.org/ns/1.0";
	public static final String TEI_NS_PREFIX = "tei";

	public static final String TEI_SIG_GE_URI = "http://www.tei-c.org/ns/geneticEditions";
	public static final String TEI_SIG_GE_PREFIX = "ge";

	public static final String SVG_NS_URI = "http://www.w3.org/2000/svg";
	public static final String SVG_NS_PREFIX = "svg";

	public CustomNamespaceMap() {
		super();
		put(URI.create(CustomNamespaceMap.FAUST_NS_URI), FAUST_NS_PREFIX);
		put(URI.create(CustomNamespaceMap.TEI_NS_URI), TEI_NS_PREFIX);
		put(URI.create(CustomNamespaceMap.TEI_SIG_GE_URI), TEI_SIG_GE_PREFIX);
		put(URI.create(CustomNamespaceMap.SVG_NS_URI), SVG_NS_PREFIX);
		put(URI.create(XMLDatabase.EXIST_NS_URI), XMLDatabase.EXIST_NS_PREFIX);
	}
}
