package de.faustedition.text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.inject.Inject;

import de.faustedition.template.TemplateRepresentationFactory;

public class TextResource extends ServerResource {

	private final TemplateRepresentationFactory viewFactory;
	private Text text;

	@Inject
	public TextResource(TemplateRepresentationFactory viewFactory) {
		this.viewFactory = viewFactory;
	}

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
