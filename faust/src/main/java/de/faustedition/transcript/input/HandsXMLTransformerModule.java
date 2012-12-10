package de.faustedition.transcript.input;

import static eu.interedition.text.Annotation.JSON;

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

public class HandsXMLTransformerModule extends AbstractAnnotationXMLTransformerModule {

	private long lastHandsChangeOffset = -1;
	private String lastHandsChangeValue = null;

	public HandsXMLTransformerModule() {
		super(1000, false);
	}

	private void addHandAnnotation(XMLTransformer transformer) {

		if(lastHandsChangeValue != null) {

			ObjectNode data = JSON.createObjectNode();
			data.put("value", lastHandsChangeValue);
			Name name = new Name(new QName(Namespaces.FAUST_NS_URI, "hand"));
			long start = lastHandsChangeOffset;
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

		if(entity.getName().getLocalName().equals("handShift")) {

			addHandAnnotation(transformer);


			JsonNode newAttribute = entity.getAttributes().get("new");

			if (newAttribute == null)
				throw new TranscriptInvalidException("Element handShift doesn't have a 'new' attribute.");

			String newValue = newAttribute.getTextValue();
			lastHandsChangeValue = newValue;
			lastHandsChangeOffset = transformer.getTextOffset();
		}
	}		

	

		@Override
		public void end(XMLTransformer transformer) {
			// TODO having to call super is a bit unclean and non-obvious
			addHandAnnotation(transformer);
			super.end(transformer);
		}
	
}
