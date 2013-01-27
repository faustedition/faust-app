package de.faustedition.transcript;

import static de.faustedition.xml.Namespaces.TEI_SIG_GE;
import static eu.interedition.text.TextConstants.TEI_NS;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.faustedition.document.MaterialUnit;
import de.faustedition.transcript.input.HandsXMLTransformerModule;
import de.faustedition.transcript.input.StageXMLTransformerModule;
import de.faustedition.xml.XMLStorage;
import eu.interedition.text.Anchor;
import eu.interedition.text.Layer;
import eu.interedition.text.Name;
import eu.interedition.text.TextRepository;
import eu.interedition.text.h2.H2TextRepository;
import eu.interedition.text.h2.LayerRelation;
import eu.interedition.text.xml.XMLTransformer;
import eu.interedition.text.xml.XMLTransformerConfigurationBase;
import eu.interedition.text.xml.XMLTransformerModule;
import eu.interedition.text.xml.module.CLIXAnnotationXMLTransformerModule;
import eu.interedition.text.xml.module.DefaultAnnotationXMLTransformerModule;
import eu.interedition.text.xml.module.LineElementXMLTransformerModule;
import eu.interedition.text.xml.module.NotableCharacterXMLTransformerModule;
import eu.interedition.text.xml.module.TEIAwareAnnotationXMLTransformerModule;
import eu.interedition.text.xml.module.TextXMLTransformerModule;

@Component
public class TextualTranscripts {

	@Autowired
	private TextRepository<JsonNode> textRepo;
	
	
	public Transcript read(Session session, XMLStorage xml, MaterialUnit materialUnit) throws IOException, XMLStreamException {
		XMLTransformer<JsonNode> transformer = createXMLTransformer (session);
		return Transcript.read(session, xml, materialUnit, textRepo, transformer);
	}
	
	private XMLTransformer<JsonNode> createXMLTransformer(Session session) {

		
		final Random random = new Random();
		
		final XMLTransformerConfigurationBase<JsonNode> conf = new XMLTransformerConfigurationBase<JsonNode>(textRepo) {

	        @Override
	        protected Layer<JsonNode> translate(Name name, Map<Name, Object> attributes, Set<Anchor> anchors) {
	        	
	            ObjectMapper mapper = new org.codehaus.jackson.map.ObjectMapper();
	            JsonNode data = mapper.valueToTree(attributes);	         
	            return new LayerRelation<JsonNode>(name, anchors, data, random.nextLong(), (H2TextRepository<JsonNode>)textRepo);
	            
	        }
	    };


		final List<XMLTransformerModule<JsonNode>> modules = conf.getModules();
		modules.add(new LineElementXMLTransformerModule<JsonNode>());
		modules.add(new NotableCharacterXMLTransformerModule<JsonNode>());
		modules.add(new TextXMLTransformerModule<JsonNode>());
		modules.add(new DefaultAnnotationXMLTransformerModule<JsonNode>());
		modules.add(new CLIXAnnotationXMLTransformerModule<JsonNode>());
		modules.add(new TEIAwareAnnotationXMLTransformerModule<JsonNode>());

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

		return new XMLTransformer<JsonNode>(conf);

	}

}
