package de.faustedition.transcript.input;

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

import javax.xml.namespace.QName;
import java.util.Map;

import static de.faustedition.xml.Namespaces.FAUST_NS_URI;
import static de.faustedition.xml.Namespaces.XML_NS_URI;


public class StXMLTransformerModule extends XMLTransformerModuleAdapter<JsonNode> {

	private static Name ST_QNAME = new Name(FAUST_NS_URI, "st");
	private static Name SPANTO_QNAME = new Name("spanTo");
	private static Name XMLID_QNAME = new Name(XML_NS_URI, "id");
	private static Name HAND_QNAME = new Name("hand");


	private class StData {

		public long startOffset;
		public String handValue;

		public StData(String handValue, long startOffset) {
			this.handValue = handValue;
			this.startOffset = startOffset;
		}
	}

	private Map<String, StData> pending = Maps.<String, StData>newHashMap();
	private XMLTransformerConfiguration<JsonNode> conf;


	public StXMLTransformerModule(XMLTransformerConfiguration<JsonNode> conf) {
		this.conf = conf;
	}

	@Override
	public void start(XMLTransformer<JsonNode> transformer, XMLEntity entity) {


		if(entity.getName().getLocalName().equals("st")
				&& entity.getAttributes().containsKey(SPANTO_QNAME)) {

			String spanTo = entity.getAttributes().get(SPANTO_QNAME).substring(1);
			String hand = entity.getAttributes().get(HAND_QNAME);
			pending.put(spanTo, new StData(hand, transformer.getTextOffset()));

		}
		if (entity.getAttributes().containsKey(XMLID_QNAME)) {
			String id = entity.getAttributes().get(XMLID_QNAME);
			if (this.pending.containsKey(id)) {
				StData stData = pending.remove(id);
				Map<Name,String> data = Maps.newHashMap();
				data.put(HAND_QNAME, stData.handValue);
				long endOffset = transformer.getTextOffset();
				Anchor<JsonNode> textTarget = new Anchor<JsonNode>(transformer.getTarget(),
						new TextRange(stData.startOffset, endOffset));
				if (stData.startOffset != endOffset)
					conf.xmlElement(ST_QNAME, data, textTarget);
			}
		}
	}
}
