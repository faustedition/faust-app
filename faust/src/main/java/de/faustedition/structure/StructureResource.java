package de.faustedition.structure;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.inject.Inject;

import de.faustedition.FaustURI;
import de.faustedition.template.TemplateRepresentationFactory;


public class StructureResource extends ServerResource {
	private final TemplateRepresentationFactory viewFactory;
	private FaustURI uri;

	@Inject
	public StructureResource(TemplateRepresentationFactory viewFactory) {
		this.viewFactory = viewFactory;
	}

	public void setURI(FaustURI uri) {
		this.uri = uri;
	}

	@Get("html")
	public Representation overview() throws IOException {
		Map<String, Object> viewModel = new HashMap<String, Object>();
		viewModel.put("uri", uri);
		
		return viewFactory.create("structure/structure", getRequest().getClientInfo(), viewModel);
	}


}
