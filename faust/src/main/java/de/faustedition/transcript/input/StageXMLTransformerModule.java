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

import static de.faustedition.xml.Namespaces.TEI_SIG_GE;

public class StageXMLTransformerModule extends XMLTransformerModuleAdapter<JsonNode> {

	private long lastStageChangeOffset = -1;
	private String lastStageChangeValue = null;
	private XMLTransformerConfiguration<JsonNode> conf;


	public StageXMLTransformerModule(XMLTransformerConfiguration<JsonNode> conf) {
		this.conf = conf;
	}

	private static Name STAGE_QNAME = new Name(TEI_SIG_GE, "stage");

	private void addStageAnnotation(XMLTransformer<JsonNode> transformer) {
		if (lastStageChangeValue != null) {
			HashMap<Name, String> data = Maps.newHashMap();
			data.put(new Name("value"), lastStageChangeValue);
			Name name = new Name(new QName(Namespaces.FAUST_NS_URI, "stage"));
			long start = lastStageChangeOffset;
			long end = transformer.getTextOffset();
			Anchor<JsonNode> textTarget = new Anchor<JsonNode>(transformer.getTarget(),
					new TextRange(start, end));

			if (start != end)
				conf.xmlElement(name, data, textTarget);
		}
	}


	@Override
	public void start(XMLTransformer<JsonNode> transformer, XMLEntity entity) {
		if (entity.getAttributes().containsKey(STAGE_QNAME)) {
			addStageAnnotation(transformer);
			lastStageChangeValue = entity.getAttributes().get(STAGE_QNAME);
			lastStageChangeOffset = transformer.getTextOffset();
		}
	}


	@Override
	public void end(XMLTransformer<JsonNode> transformer) {
		addStageAnnotation(transformer);
	}

}
