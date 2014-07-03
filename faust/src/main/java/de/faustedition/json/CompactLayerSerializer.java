/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.faustedition.json;

import de.faustedition.xml.CustomNamespaceMap;
import eu.interedition.text.Layer;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
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
    		String prefix = CustomNamespaceMap.INSTANCE.get(URI.create(ns));
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
