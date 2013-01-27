package de.faustedition.transcript.input;

import static eu.interedition.text.Annotation.JSON;
import static de.faustedition.xml.Namespaces.TEI_SIG_GE;


import javax.xml.namespace.QName;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import de.faustedition.xml.Namespaces;
import eu.interedition.text.Annotation;
import eu.interedition.text.Name;
import eu.interedition.text.TextTarget;
import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLTransformer;
import eu.interedition.text.xml.module.AbstractAnnotationXMLTransformerModule;

public class StageXMLTransformerModule extends AbstractAnnotationXMLTransformerModule {

	private long lastStageChangeOffset = -1;
	private String lastStageChangeValue = null;

	public StageXMLTransformerModule() {
		super(1000, false);
	}

	private static String STAGE_QNAME = "{" + TEI_SIG_GE + "}" +"stage"; 
	private void addStageAnnotation(XMLTransformer transformer) {

		if(lastStageChangeValue != null) {

			ObjectNode data = JSON.createObjectNode();
			data.put("value", lastStageChangeValue);
			Name name = new Name(new QName(Namespaces.FAUST_NS_URI, "stage"));
			long start = lastStageChangeOffset;
			long end = transformer.getTextOffset();
			TextTarget textTarget = new TextTarget(transformer.getTarget(), start, end);
			Annotation annotation = new Annotation(name, textTarget, data);

			annotation.setData(data);
			
			if (start != end)
				add(transformer, annotation);
		}
	}


	@Override
	public void start(XMLTransformer transformer, XMLEntity entity) {

		if (entity.getAttributes().has(STAGE_QNAME)) {
			
			addStageAnnotation(transformer);

			JsonNode newAttribute = entity.getAttributes().get(STAGE_QNAME);

			String newValue = newAttribute.getTextValue();
			lastStageChangeValue = newValue;
			lastStageChangeOffset = transformer.getTextOffset();
		}
	}		

	

		@Override
		public void end(XMLTransformer transformer) {
			// TODO having to call super is a bit unclean and non-obvious
			addStageAnnotation(transformer);
			super.end(transformer);
		}
	
}
