package de.faustedition.transcript;


import static de.faustedition.xml.Namespaces.TEI_SIG_GE;
import static eu.interedition.text.TextConstants.TEI_NS;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.codehaus.jackson.JsonNode;
import org.hibernate.Session;

import de.faustedition.document.MaterialUnit;
import de.faustedition.xml.XMLStorage;
import de.faustedition.transcript.input.FacsimilePathXMLTransformerModule;
import de.faustedition.transcript.input.HandsXMLTransformerModule;
import eu.interedition.text.Anchor;
import eu.interedition.text.Layer;
import eu.interedition.text.Name;
import eu.interedition.text.TextRepository;
import eu.interedition.text.simple.KeyValues;
import eu.interedition.text.simple.SimpleLayer;
import eu.interedition.text.xml.XMLTransformer;
import eu.interedition.text.xml.XMLTransformerConfiguration;
import eu.interedition.text.xml.XMLTransformerConfigurationBase;
import eu.interedition.text.xml.XMLTransformerModule;
import eu.interedition.text.xml.module.CLIXAnnotationXMLTransformerModule;
import eu.interedition.text.xml.module.DefaultAnnotationXMLTransformerModule;
import eu.interedition.text.xml.module.LineElementXMLTransformerModule;
import eu.interedition.text.xml.module.NotableCharacterXMLTransformerModule;
import eu.interedition.text.xml.module.TEIAwareAnnotationXMLTransformerModule;
import eu.interedition.text.xml.module.TextXMLTransformerModule;

public class DocumentaryTranscripts {
	
	
	public static Transcript read(Session session, XMLStorage xml, MaterialUnit materialUnit) throws IOException, XMLStreamException {
		XMLTransformer transformer = createXMLTransformer (session, materialUnit);
		return Transcript.read(session, xml, materialUnit, transformer);
	}
	
	private static XMLTransformer createXMLTransformer(Session session, MaterialUnit materialUnit) {
		
		final XMLTransformerConfigurationBase conf = new XMLTransformerConfigurationBase<KeyValues>(repository) {

	        @Override
	        protected Layer<KeyValues> translate(Name name, Map<Name, Object> attributes, Set<Anchor> anchors) {
	            final KeyValues kv = new KeyValues();
	            for (Map.Entry<Name, Object> attr : attributes.entrySet()) {
	                kv.put(attr.getKey().toString(), attr.getValue());
	            }
	            return new SimpleLayer<KeyValues>(name, "", kv, anchors);
	        }
	    };

			

		final List<XMLTransformerModule> modules = conf.getModules();
		modules.add(new LineElementXMLTransformerModule());
		modules.add(new NotableCharacterXMLTransformerModule());
		modules.add(new TextXMLTransformerModule());
		modules.add(new DefaultAnnotationXMLTransformerModule());
		modules.add(new CLIXAnnotationXMLTransformerModule());
		modules.add(new HandsXMLTransformerModule(conf));
		modules.add(new FacsimilePathXMLTransformerModule(materialUnit));
		modules.add(new TEIAwareAnnotationXMLTransformerModule());

		
		conf.addLineElement(new Name(TEI_SIG_GE, "document"));
		conf.addLineElement(new Name(TEI_SIG_GE, "line"));

		conf.addContainerElement(new Name(TEI_NS, "text"));
		conf.addContainerElement(new Name(TEI_NS, "div"));
		conf.addContainerElement(new Name(TEI_NS, "lg"));
		conf.addContainerElement(new Name(TEI_NS, "subst"));
		conf.addContainerElement(new Name(TEI_NS, "choice"));
		conf.addContainerElement(new Name(TEI_NS, "zone"));

		conf.exclude(new Name(TEI_NS, "teiHeader"));
		return new XMLTransformer(session, conf);
	}

}
