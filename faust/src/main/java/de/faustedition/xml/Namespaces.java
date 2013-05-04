package de.faustedition.xml;

import java.net.URI;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface Namespaces {
	String FAUST_NS_URI = "http://www.faustedition.net/ns";
	String FAUST_NS_PREFIX = "f";

	String TEI_NS_URI = "http://www.tei-c.org/ns/1.0";
	String TEI_NS_PREFIX = "tei";

	String TEI_SIG_GE_URI = "http://www.tei-c.org/ns/geneticEditions";
	String TEI_SIG_GE_PREFIX = "ge";
	URI TEI_SIG_GE = URI.create(TEI_SIG_GE_URI);

	String SVG_NS_URI = "http://www.w3.org/2000/svg";
	String SVG_NS_PREFIX = "svg";

	String XLINK_NS_URI = "http://www.w3.org/1999/xlink";
	String XLINK_NS_PREFIX = "xlink";
}
