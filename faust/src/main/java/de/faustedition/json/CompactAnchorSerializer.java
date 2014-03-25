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
