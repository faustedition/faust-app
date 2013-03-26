package de.faustedition.xml;

import static de.faustedition.xml.Namespaces.TEI_NS_PREFIX;

import java.net.URI;

import org.goddag4j.io.NamespaceMap;

@SuppressWarnings("serial")
public class CustomNamespaceMap extends NamespaceMap implements Namespaces {
	public static final CustomNamespaceMap INSTANCE = new CustomNamespaceMap();

	public CustomNamespaceMap() {
		super();
		put(URI.create(Namespaces.FAUST_NS_URI), FAUST_NS_PREFIX);
		put(URI.create(Namespaces.TEI_NS_URI), TEI_NS_PREFIX);
		put(URI.create(Namespaces.TEI_SIG_GE_URI), TEI_SIG_GE_PREFIX);
		put(URI.create(Namespaces.SVG_NS_URI), SVG_NS_PREFIX);
		put(URI.create(Namespaces.XLINK_NS_URI), XLINK_NS_PREFIX);
		put(URI.create(Namespaces.XML_NS_URI), XML_NS_PREFIX);
	}
}
