package de.faustedition.transcript.input;

import java.util.HashMap;
import java.util.HashSet;

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

public class HandsXMLTransformerModule<T> extends XMLTransformerModuleAdapter<T> {

	private long lastHandsChangeOffset = -1;
	private String lastHandsChangeValue = null;
	private XMLTransformerConfiguration conf;

	public HandsXMLTransformerModule(XMLTransformerConfiguration conf) {
		this.conf = conf;
	}

	private void addHandAnnotation(XMLTransformer transformer) {

		if(lastHandsChangeValue != null) {
			
			HashMap<Object, Object> data = Maps.newHashMap();
			data.put("value", lastHandsChangeValue);
			Name name = new Name(new QName(Namespaces.FAUST_NS_URI, "hand"));
			long start = lastHandsChangeOffset;
			long end = transformer.getTextOffset();
			Anchor textTarget = new Anchor(transformer.getTarget(), 
					new TextRange(start, end));
			HashSet<Anchor> anchors = Sets.newHashSet(textTarget);
			//Layer annotation = new SimpleLayer<JsonNode>(name, "", data, anchors);
			
			conf.xmlElement(name, data, textTarget);
			//add(transformer, annotation);
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
