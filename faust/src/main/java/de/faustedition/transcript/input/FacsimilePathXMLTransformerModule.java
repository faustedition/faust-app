package de.faustedition.transcript.input;

import de.faustedition.FaustURI;
import de.faustedition.document.MaterialUnit;
import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLTransformer;
import eu.interedition.text.xml.module.TextXMLTransformerModule;

public class FacsimilePathXMLTransformerModule extends TextXMLTransformerModule{

	private boolean read = false;
	private MaterialUnit materialUnit;

	public FacsimilePathXMLTransformerModule(MaterialUnit materialUnit) {
		this.materialUnit = materialUnit;
	}

	@Override
	public void end(XMLTransformer transformer, XMLEntity entity) {

		if(entity.getName().getLocalName().equals("graphic")  
				&&	!entity.getAttributes().containsKey("mimeType")
				&& !read) {
			if(entity.getAttributes().containsKey("url")) {
				String url = entity.getAttributes().get("url").toString();
				try {
					materialUnit.setFacsimile(FaustURI.parse(url));
					read = true;
				} catch (IllegalArgumentException e){
					throw new TranscriptInvalidException("Invalid facsimile URI in transcript!");
				}
			}
		}
	}		
}
