package de.faustedition.transcript.input;

import java.util.HashMap;
import java.util.HashSet;

import javax.xml.namespace.QName;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.faustedition.xml.Namespaces;
import eu.interedition.text.Anchor;
import eu.interedition.text.Name;
import eu.interedition.text.TextRange;
import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLTransformer;
import eu.interedition.text.xml.XMLTransformerConfiguration;
import eu.interedition.text.xml.module.XMLTransformerModuleAdapter;

public class HandsXMLTransformerModule<T> extends XMLTransformerModuleAdapter<T> {

	private long lastHandsChangeOffset = -1;
	private String lastHandsChangeValue = null;
	private XMLTransformerConfiguration<JsonNode> conf;
	private ObjectMapper objectMapper;

	public HandsXMLTransformerModule(XMLTransformerConfiguration<JsonNode> conf, ObjectMapper objectMapper) {
		this.conf = conf;
		this.objectMapper = objectMapper;

	}

	private void addHandAnnotation(XMLTransformer transformer) {

		if(lastHandsChangeValue != null) {
			
			ObjectNode data = objectMapper.createObjectNode();
			data.put("value", lastHandsChangeValue);
			Name name = new Name(new QName(Namespaces.FAUST_NS_URI, "hand"));
			long start = lastHandsChangeOffset;
			long end = transformer.getTextOffset();
			Anchor textTarget = new Anchor(transformer.getTarget(), 
					new TextRange(start, end));
			conf.add(name, data, textTarget);
		}
	}


	@Override
	public void start(XMLTransformer<T> transformer, XMLEntity entity) {

		if(entity.getName().getLocalName().equals("handShift")) {

			addHandAnnotation(transformer);

			Object newAttribute = entity.getAttributes().get("new");

			if (newAttribute == null)
				throw new TranscriptInvalidException("Element handShift doesn't have a 'new' attribute.");

			String newValue = (String) newAttribute;
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
