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

public class WhitespaceXMLTransformerModule extends XMLTransformerModuleAdapter<JsonNode> {


	private XMLTransformerConfiguration<JsonNode> conf;


	public WhitespaceXMLTransformerModule(XMLTransformerConfiguration<JsonNode> conf) {
		this.conf = conf;
	}

	@Override
	public void startText(XMLTransformer<JsonNode> transformer) {
		super.startText(transformer);    //To change body of overridden methods use File | Settings | File Templates.

	}

	@Override
	public void text(XMLTransformer<JsonNode> transformer, String text) {
		super.text(transformer, text);    //To change body of overridden methods use File | Settings | File Templates.
	}
}
