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

import de.faustedition.FaustURI;
import de.faustedition.document.MaterialUnit;
import eu.interedition.text.Name;
import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLTransformer;
import eu.interedition.text.xml.module.XMLTransformerModuleAdapter;
import org.codehaus.jackson.JsonNode;

public class FacsimilePathXMLTransformerModule extends XMLTransformerModuleAdapter<JsonNode> {

	private boolean read = false;
	private final MaterialUnit materialUnit;

	public FacsimilePathXMLTransformerModule(MaterialUnit materialUnit) {
		this.materialUnit = materialUnit;
	}

	@Override
	public void end(XMLTransformer transformer, XMLEntity entity) {
		if (read || !entity.getName().getLocalName().equals("graphic") || entity.getAttributes().containsKey(new Name("mimeType"))) {
			return;
		}
		final String url = entity.getAttributes().get(new Name("url"));
		if (url == null) {
			return;
		}
		try {
			materialUnit.setFacsimile(FaustURI.parse(url));
			read = true;
		} catch (IllegalArgumentException e){
			throw new TranscriptInvalidException("Invalid facsimile URI in transcript!");
		}
	}
}
