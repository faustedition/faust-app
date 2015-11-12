/*
 * Copyright (c) 2015 Faust Edition development team.
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

import com.google.common.collect.ImmutableMap;
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
import java.util.HashMap;
import java.util.Map;

public class GXMLTransformerModule extends XMLTransformerModuleAdapter<JsonNode> {

	//private XMLTransformerConfiguration<JsonNode> conf;

	private ImmutableMap<String, String> refCharacterMapping = ImmutableMap.<String,String>builder()
			.put("#ditto-line", "")
			.put("#ditto-quote", "")
			.put("#g_break", "[") // TODO: implement custom glyph
			.put("#g_transp_1", "⊢")
			.put("#g_transp_2", "⊨")
			.put("#g_transp_2a", "⫢")
			.put("#g_transp_3", "⫢")
			.put("#g_transp_3S", "⎱")
			// .put("#g_transp_4", "") // TODO: implement custom glyph
			.put("#g_transp_5", "✓")
			.put("#g_transp_6", "#")
			.put("#g_transp_7", "◶")
			.put("#g_transp_8", "⊣")
			.put("#parenthesis_left", "(")
			.put("#parenthesis_right", ")")
			.put("#truncation", ".")
			.build();

	@Override
	public void start(XMLTransformer<JsonNode> transformer, XMLEntity entity) {

		if(entity.getName().getLocalName().equals("g")) {
			Map<Name, String> attributes = entity.getAttributes();
			if (attributes.containsKey(new Name("ref"))) {
				String refValue = attributes.get(new Name("ref"));
				if (refCharacterMapping.containsKey(refValue)) {
					transformer.write(refCharacterMapping.get(refValue), false);
				} else {
					transformer.write("�", false);
				}
			}
		}
	}
}
