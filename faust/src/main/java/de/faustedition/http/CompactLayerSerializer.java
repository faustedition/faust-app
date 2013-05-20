package de.faustedition.http;

import de.faustedition.xml.Namespaces;
import eu.interedition.text.Layer;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
class CompactLayerSerializer extends JsonSerializer<Layer> {

    @Override
    public Class<Layer> handledType() {
        return Layer.class;
    }

    private String prefixNS(String attributeName) {    
    	int nsEnd = attributeName.indexOf('}');
    	if (nsEnd >= 0) {
    		String ns = attributeName.substring(1, nsEnd);
    		String simpleName = attributeName.substring(nsEnd + 1);
    		String prefix = Namespaces.INSTANCE.getPrefix(ns);
    		return prefix + ":" + simpleName;
    	} else {
    		return attributeName;
    	}
    }
    
    @Override
    public void serialize(Layer value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();

        jgen.writeObjectField("n", value.getName().hashCode());
        
        jgen.writeObjectField("t", value.getAnchors());

        Map<String, JsonNode> data = new HashMap<String, JsonNode>();
        if (value.data() != null) {
        	Iterator<Entry<String, JsonNode>> fields = ((ObjectNode)(value.data())).getFields();
        	while (fields.hasNext()) {
        		Entry<String, JsonNode> next = fields.next();
        		data.put(prefixNS(next.getKey()), next.getValue());
        	}
        }
    	jgen.writeObjectField("d", data);
        
        try {
            jgen.writeObjectField("id", value.getClass().getMethod("getId").invoke(value));
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
        } catch (IllegalAccessException e) {
        }

        jgen.writeEndObject();
    }
}
