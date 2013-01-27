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

public class StageXMLTransformerModule<T> extends XMLTransformerModuleAdapter<T> {

	private long lastStageChangeOffset = -1;
	private String lastStageChangeValue = null;
	private XMLTransformerConfiguration conf;


	public StageXMLTransformerModule(XMLTransformerConfiguration conf) {
		this.conf = conf;
	}

	private static String STAGE_QNAME = "{" + TEI_SIG_GE + "}" +"stage"; 
	private void addStageAnnotation(XMLTransformer transformer) {

		if(lastStageChangeValue != null) {

			HashMap<Name, Object> data = Maps.newHashMap();
			data.put(new Name((String)null, "value"), lastStageChangeValue);
			Name name = new Name(new QName(Namespaces.FAUST_NS_URI, "stage"));
			long start = lastStageChangeOffset;
			long end = transformer.getTextOffset();
			Anchor textTarget = new Anchor(transformer.getTarget(), 
					new TextRange(start, end));
			
			if (start != end)
				conf.xmlElement(name, data, textTarget);
		}
	}


	@Override
	public void start(XMLTransformer transformer, XMLEntity entity) {

		
		if (entity.getAttributes().containsKey(STAGE_QNAME)) {
			
			addStageAnnotation(transformer);

			Object newAttribute = entity.getAttributes().get(STAGE_QNAME);

			String newValue = (String) newAttribute;
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
