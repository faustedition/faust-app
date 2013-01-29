package de.faustedition.transcript.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.namespace.QName;

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
import org.codehaus.jackson.JsonNode;

public class HandsXMLTransformerModule extends XMLTransformerModuleAdapter<JsonNode> {

	private long lastHandsChangeOffset = -1;
	private String lastHandsChangeValue = null;
	private XMLTransformerConfiguration<JsonNode> conf;


	public HandsXMLTransformerModule(XMLTransformerConfiguration<JsonNode> conf) {
		this.conf = conf;
	}

	private void addHandAnnotation(XMLTransformer<JsonNode> transformer) {

		if(lastHandsChangeValue != null) {
			
			Map<Name,Object> data = Maps.newHashMap();
			data.put(new Name((String)null, "value"), lastHandsChangeValue);
			Name name = new Name(new QName(Namespaces.FAUST_NS_URI, "hand"));
			long start = lastHandsChangeOffset;
			long end = transformer.getTextOffset();
			Anchor textTarget = new Anchor(transformer.getTarget(), 
					new TextRange(start, end));
			if (start != end)
				conf.xmlElement(name, data, textTarget);
		}
	}


	@Override
	public void start(XMLTransformer<JsonNode> transformer, XMLEntity entity) {

		if(entity.getName().getLocalName().equals("handShift")) {

			addHandAnnotation(transformer);

			Object newAttribute = entity.getAttributes().get(new Name((String)null, "new"));

			if (newAttribute == null)
				throw new TranscriptInvalidException("Element handShift doesn't have a 'new' attribute.");

			String newValue = (String) newAttribute;
			lastHandsChangeValue = newValue;
			lastHandsChangeOffset = transformer.getTextOffset();
		}
	}		

	

		@Override
		public void end(XMLTransformer<JsonNode> transformer) {
			// TODO having to call super is a bit unclean and non-obvious
			addHandAnnotation(transformer);
			super.end(transformer);
		}
	
}
