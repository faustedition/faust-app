package de.faustedition.transcript;

import static de.faustedition.xml.Namespaces.TEI_SIG_GE;
import static eu.interedition.text.TextConstants.TEI_NS;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.hibernate.Session;

import de.faustedition.document.MaterialUnit;
import de.faustedition.xml.XMLStorage;
import eu.interedition.text.Name;
import eu.interedition.text.util.SimpleXMLTransformerConfiguration;
import eu.interedition.text.xml.XMLTransformer;
import eu.interedition.text.xml.XMLTransformerModule;
import eu.interedition.text.xml.module.CLIXAnnotationXMLTransformerModule;
import eu.interedition.text.xml.module.DefaultAnnotationXMLTransformerModule;
import eu.interedition.text.xml.module.LineElementXMLTransformerModule;
import eu.interedition.text.xml.module.NotableCharacterXMLTransformerModule;
import eu.interedition.text.xml.module.TEIAwareAnnotationXMLTransformerModule;
import eu.interedition.text.xml.module.TextXMLTransformerModule;

public class TextualTranscripts {
	
	
	public static Transcript read(Session session, XMLStorage xml, MaterialUnit materialUnit) throws IOException, XMLStreamException {
		XMLTransformer transformer = createXMLTransformer (session);
		return Transcript.read(session, xml, materialUnit, transformer);
	}
	
	private static XMLTransformer createXMLTransformer(Session session) {
		final SimpleXMLTransformerConfiguration conf = new SimpleXMLTransformerConfiguration();

		final List<XMLTransformerModule> modules = conf.getModules();
		modules.add(new LineElementXMLTransformerModule());
		modules.add(new NotableCharacterXMLTransformerModule());
		modules.add(new TextXMLTransformerModule());
		modules.add(new DefaultAnnotationXMLTransformerModule());
		modules.add(new CLIXAnnotationXMLTransformerModule());
		modules.add(new TEIAwareAnnotationXMLTransformerModule());

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
		conf.addLineElement(new Name(TEI_SIG_GE, "line"));

		conf.addContainerElement(new Name(TEI_NS, "text"));
		conf.addContainerElement(new Name(TEI_NS, "div"));
		conf.addContainerElement(new Name(TEI_NS, "lg"));
		conf.addContainerElement(new Name(TEI_NS, "subst"));
		conf.addContainerElement(new Name(TEI_NS, "choice"));
		conf.addContainerElement(new Name(TEI_NS, "zone"));

		conf.exclude(new Name(TEI_NS, "teiHeader"));
		conf.exclude(new Name(TEI_NS, "front"));
		conf.exclude(new Name(TEI_NS, "fw"));
		conf.exclude(new Name(TEI_NS, "app"));

		conf.include(new Name(TEI_NS, "lem"));

		return new XMLTransformer(session, conf);
	}

}
