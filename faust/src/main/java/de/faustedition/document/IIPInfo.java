package de.faustedition.document;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class IIPInfo {

	protected String secret;
	protected String ident;
	protected Map<String, String> properties; 
	protected int width;
	protected int height;

	@Inject
	public IIPInfo(@Named("facsimile.iip.url") String imageServerUrl, 
			@Named("auth.ident") String ident, @Named("auth.secret") String secret) {
		this.ident = ident;
		this.secret = secret;
	}

	public void retrieve(String facsimileUrl) throws ResourceException, IOException {

		ClientResource cr = new ClientResource(facsimileUrl + "&obj=IIP,1.0&obj=Max-size&obj=Tile-size&obj=Resolution-number");
		cr.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, ident, secret));
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
