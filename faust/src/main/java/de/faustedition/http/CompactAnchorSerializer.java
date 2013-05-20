package de.faustedition.http;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import eu.interedition.text.Anchor;
import eu.interedition.text.Text;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class CompactAnchorSerializer extends JsonSerializer<Anchor> {

    @Override
    public Class<Anchor> handledType() {
        return Anchor.class;
    }

    @Override
    public void serialize(Anchor value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {

    	jgen.writeStartArray();

    	jgen.writeNumber(value.getRange().getStart());
    	jgen.writeNumber(value.getRange().getEnd());
    	
        final Text text = value.getText();
        try {
        	text.hashCode();
            jgen.writeNumber((Long)(text.getClass().getMethod("getId").invoke(text)));
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
        } catch (IllegalAccessException e) {
        }
        
        jgen.writeEndArray();
    }
}
