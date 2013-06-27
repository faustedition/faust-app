package de.faustedition.document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.faustedition.FaustURI;
import de.faustedition.JsonRespresentation;
import de.faustedition.template.TemplateRepresentationFactory;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DocumentResource extends ServerResource {

	@Autowired
	private TemplateRepresentationFactory viewFactory;

	private Document document;
	
	private FaustURI path;

	public void setDocument(Document document) {
		this.document = document;
	}

	public void setPath(FaustURI path) {
		this.path = path;
	}
	
	@Get("html")
	public Representation overview() throws IOException {
		Map<String, Object> viewModel = new HashMap<String, Object>();
		viewModel.put("document", document);
		viewModel.put("contents", document.getSortedContents());
		viewModel.put("path", path.toString());

		Map<String, String> params = this.getRequest().getResourceRef().getQueryAsForm().getValuesMap();
		if ("transcript-bare".equals(params.get("view"))) {
			return viewFactory.create("document/document-transcript-bare", getRequest().getClientInfo(), viewModel);
		} else {				
			return viewFactory.create("document/document-app", getRequest().getClientInfo(), viewModel);
		}
	}
	
	@Get("svg")
	public Representation graphic() {
		String result = "<xml version=\"1.0\"?><svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">"
			+ "<script type=\"text/javascript\" ></script>"
			+ "</svg>";
		
		
		return new StringRepresentation(result, MediaType.IMAGE_SVG,
				Language.DEFAULT, CharacterSet.UTF_8);
	}
	
	@Get("json")
	public Representation documentStructure() {
		return new JsonRespresentation() {

			@Override
			protected void generate() throws IOException {
				generate(document);
			}

			protected void generate(MaterialUnit unit) throws IOException {
				generator.writeStartObject();

				generator.writeStringField("type", unit.getType().name().toLowerCase());
				if (unit instanceof Document) {
					generator.writeStringField("name", unit.toString());
				}
				generator.writeNumberField("order", unit.getOrder());
				generator.writeNumberField("id", unit.node.getId());
//				final GoddagTranscript transcript = unit.getTranscript();
				if (unit.getTranscriptSource() != null) {
					generator.writeObjectFieldStart("transcript");
//					generator.writeStringField("type", transcript.getType().name().toLowerCase());
					generator.writeStringField("source", unit.getTranscriptSource().toString());
//					if (transcript.getType() == TranscriptType.DOCUMENTARY) {
//						DocumentaryGoddagTranscript dt = (DocumentaryGoddagTranscript) transcript;
//						generator.writeArrayFieldStart("facsimiles");
//						for (FaustURI facsimile : dt.getFacsimileReferences()) {
//							generator.writeString(facsimile.toString());
//						}
//						generator.writeEndArray();
//					}
					generator.writeEndObject();
					final FaustURI facsimile = unit.getFacsimile();
					if (facsimile != null) 
							generator.writeStringField("facsimile", facsimile.toString());
					


				}

				generator.writeArrayFieldStart("contents");
				for (MaterialUnit content : new TreeSet<MaterialUnit>(unit)) {
					generate(content);
				}
				generator.writeEndArray();

				generator.writeEndObject();
			}
		};
	}

	
}
