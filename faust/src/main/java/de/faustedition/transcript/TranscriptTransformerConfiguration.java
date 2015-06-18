package de.faustedition.transcript;

import eu.interedition.text.Name;
import eu.interedition.text.xml.XMLTransformerConfigurationBase;
import eu.interedition.text.xml.XMLTransformerModule;
import eu.interedition.text.xml.module.*;
import org.codehaus.jackson.JsonNode;

import java.util.List;

import static de.faustedition.xml.Namespaces.TEI_SIG_GE;
import static eu.interedition.text.TextConstants.TEI_NS;

/**
 * User: moz
 * Date: 18/06/15
 * Time: 12:22 PM
 */
public class TranscriptTransformerConfiguration {
	public static void configure(XMLTransformerConfigurationBase<JsonNode> conf) {
		conf.addLineElement(new Name(TEI_NS, "text"));
		conf.addLineElement(new Name(TEI_NS, "div"));
		conf.addLineElement(new Name(TEI_NS, "head"));
		conf.addLineElement(new Name(TEI_NS, "sp"));
		conf.addLineElement(new Name(TEI_NS, "stage"));
		conf.addLineElement(new Name(TEI_NS, "speaker"));
		conf.addLineElement(new Name(TEI_NS, "lg"));
		conf.addLineElement(new Name(TEI_NS, "l"));
		conf.addLineElement(new Name(TEI_NS, "p"));
		conf.addLineElement(new Name(TEI_NS, "ab"));
		conf.addLineElement(new Name(TEI_NS, "line"));
		conf.addLineElement(new Name(TEI_SIG_GE, "document"));

		conf.addContainerElement(new Name(TEI_NS, "text"));
		conf.addContainerElement(new Name(TEI_NS, "div"));
		conf.addContainerElement(new Name(TEI_NS, "lg"));
		conf.addContainerElement(new Name(TEI_NS, "subst"));
		conf.addContainerElement(new Name(TEI_NS, "choice"));
		conf.addContainerElement(new Name(TEI_NS, "zone"));

		conf.addWhitespaceTrimmingElement(new Name(TEI_SIG_GE, "line"));
		conf.setCompressingWhitespace(false);

		conf.exclude(new Name(TEI_NS, "teiHeader"));
		conf.exclude(new Name(TEI_NS, "front"));
		conf.exclude(new Name(TEI_NS, "app"));

		conf.include(new Name(TEI_NS, "lem"));


		final List<XMLTransformerModule<JsonNode>> modules = conf.getModules();
		modules.add(new LineElementXMLTransformerModule<JsonNode>());
		modules.add(new NotableCharacterXMLTransformerModule<JsonNode>());
		modules.add(new TextXMLTransformerModule<JsonNode>());
		modules.add(new DefaultAnnotationXMLTransformerModule<JsonNode>());
		modules.add(new CLIXAnnotationXMLTransformerModule<JsonNode>());
	}
}
