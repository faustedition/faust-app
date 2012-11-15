package de.faustedition.transcript.input;

import de.faustedition.FaustURI;
import de.faustedition.document.MaterialUnit;
import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLTransformer;
import eu.interedition.text.xml.module.AbstractAnnotationXMLTransformerModule;

public class FacsimilePathXMLTransformerModule extends AbstractAnnotationXMLTransformerModule {

	private boolean read = false;
	private MaterialUnit materialUnit;

	public FacsimilePathXMLTransformerModule(MaterialUnit materialUnit) {
		super(1000, false);
		this.materialUnit = materialUnit;
	}

	@Override
	public void end(XMLTransformer transformer, XMLEntity entity) {

		if(entity.getName().getLocalName().equals("graphic")  
				&&	!entity.getAttributes().has("mimeType")
				&& !read) {
			if(entity.getAttributes().has("url")) {
				String url = entity.getAttributes().get("url").getTextValue();
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
