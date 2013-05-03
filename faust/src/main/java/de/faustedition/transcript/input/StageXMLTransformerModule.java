package de.faustedition.transcript.input;

import static de.faustedition.xml.Namespaces.TEI_SIG_GE;

import java.util.HashMap;

import javax.xml.namespace.QName;

import com.google.common.collect.Maps;

import de.faustedition.xml.Namespaces;
import eu.interedition.text.Anchor;
import eu.interedition.text.Name;
import eu.interedition.text.TextRange;
import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLTransformer;
import eu.interedition.text.xml.XMLTransformerConfiguration;
import eu.interedition.text.xml.module.XMLTransformerModuleAdapter;
import org.codehaus.jackson.JsonNode;

public class StageXMLTransformerModule extends XMLTransformerModuleAdapter<JsonNode> {

	private long lastStageChangeOffset = -1;
	private String lastStageChangeValue = null;
	private XMLTransformerConfiguration<JsonNode> conf;


	public StageXMLTransformerModule(XMLTransformerConfiguration<JsonNode> conf) {
		this.conf = conf;
	}

	private static Name STAGE_QNAME = new Name(TEI_SIG_GE, "stage");

	private void addStageAnnotation(XMLTransformer<JsonNode> transformer) {
		if (lastStageChangeValue != null) {
			HashMap<Name, String> data = Maps.newHashMap();
			data.put(new Name("value"), lastStageChangeValue);
			Name name = new Name(new QName(Namespaces.FAUST_NS_URI, "stage"));
			long start = lastStageChangeOffset;
			long end = transformer.getTextOffset();
			Anchor<JsonNode> textTarget = new Anchor<JsonNode>(transformer.getTarget(),
					new TextRange(start, end));

			if (start != end)
				conf.xmlElement(name, data, textTarget);
		}
	}


	@Override
	public void start(XMLTransformer<JsonNode> transformer, XMLEntity entity) {
		if (entity.getAttributes().containsKey(STAGE_QNAME)) {
			addStageAnnotation(transformer);
			lastStageChangeValue = entity.getAttributes().get(STAGE_QNAME);
			lastStageChangeOffset = transformer.getTextOffset();
		}
	}


	@Override
	public void end(XMLTransformer<JsonNode> transformer) {
		addStageAnnotation(transformer);
	}

}
