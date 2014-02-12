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

package de.faustedition.document;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class IIPInfo implements InitializingBean {

	@Autowired
	protected Environment environment;

	protected String secret;
	protected String ident;
	protected Map<String, String> properties; 
	protected int width;
	protected int height;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.ident = environment.getRequiredProperty("auth.ident");
		this.secret = environment.getRequiredProperty("auth.secret");
	}

	public void retrieve(String facsimileUrl) throws ResourceException, IOException {
		ClientResource cr = new ClientResource(facsimileUrl + "&obj=IIP,1.0&obj=Max-size&obj=Tile-size&obj=Resolution-number");
		cr.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, ident, secret));
		cr.setRetryAttempts(4);
		cr.setRetryOnError(true);
		BufferedReader br = new BufferedReader(cr.get().getReader());


		properties = new HashMap<String, String>();

		String line;
		while ((line = br.readLine()) != null) {
			String [] tokens = line.split(":");
			if (tokens.length != 2)
				throw new IOException("IIP Server response is malformed.");
			else
				properties.put(tokens[0], tokens[1]);
		}
		br.close();
		
		
		String[] dimensions = properties.get("Max-size").split("\\s+");

		if(dimensions.length != 2)
			throw new IOException("IIP Server response is malformed.");
		
		try {
			width = Integer.parseInt(dimensions[0]);
			height = Integer.parseInt(dimensions[1]);

		} catch(NumberFormatException e) {
			throw new IOException("IIP Server response is malformed.", e);
		}
		
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

}
