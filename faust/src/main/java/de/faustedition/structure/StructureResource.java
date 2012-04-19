package de.faustedition.structure;

import de.faustedition.FaustURI;
import de.faustedition.template.TemplateRepresentationFactory;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class StructureResource extends ServerResource {

	@Autowired
	private TemplateRepresentationFactory viewFactory;

	private FaustURI uri;

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
