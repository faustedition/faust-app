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
			
			Map<Name,String> data = Maps.newHashMap();
			data.put(new Name("value"), lastHandsChangeValue);
			Name name = new Name(new QName(Namespaces.FAUST_NS_URI, "hand"));
			long start = lastHandsChangeOffset;
			long end = transformer.getTextOffset();
			Anchor<JsonNode> textTarget = new Anchor<JsonNode>(transformer.getTarget(),
					new TextRange(start, end));
			if (start != end)
				conf.xmlElement(name, data, textTarget);
		}
	}


	@Override
	public void start(XMLTransformer<JsonNode> transformer, XMLEntity entity) {

		if(entity.getName().getLocalName().equals("handShift")) {

			addHandAnnotation(transformer);

			Object newAttribute = entity.getAttributes().get(new Name("new"));

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
