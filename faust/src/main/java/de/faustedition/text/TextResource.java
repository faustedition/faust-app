package de.faustedition.text;

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
public class TextResource extends ServerResource {

	@Autowired
	private TemplateRepresentationFactory viewFactory;

	private Text text;

	public void setText(Text text) {
		this.text = text;
	}

	@Get("html")
	public Representation page() throws IOException {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("text", text);
		return viewFactory.create("text/text", getClientInfo(), model);
	}
}
