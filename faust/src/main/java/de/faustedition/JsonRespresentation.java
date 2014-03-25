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

package de.faustedition;

import java.io.IOException;
import java.io.OutputStream;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;

public abstract class JsonRespresentation extends OutputRepresentation {

	protected JsonGenerator generator;

	public JsonRespresentation() {
		super(MediaType.APPLICATION_JSON);
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		setCharacterSet(CharacterSet.UTF_8);
		generator = new JsonFactory().createJsonGenerator(outputStream, JsonEncoding.UTF8);
		try {
			generate();
		} finally {
			generator.close();
		}
	}

	protected abstract void generate() throws IOException;
}
