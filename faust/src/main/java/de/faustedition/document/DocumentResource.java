package de.faustedition.document;

import de.faustedition.FaustURI;
import de.faustedition.JsonRespresentation;
import de.faustedition.template.TemplateRepresentationFactory;
import de.faustedition.transcript.DocumentaryGoddagTranscript;
import de.faustedition.transcript.GoddagTranscript;
import de.faustedition.transcript.TranscriptType;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

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

		return viewFactory.create("document/document-app", getRequest().getClientInfo(), viewModel);
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
