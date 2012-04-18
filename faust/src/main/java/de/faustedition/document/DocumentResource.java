package de.faustedition.document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.inject.Inject;

import de.faustedition.FaustURI;
import de.faustedition.JsonRespresentation;
import de.faustedition.graph.GraphDatabaseTransactional;
import de.faustedition.template.TemplateRepresentationFactory;
import de.faustedition.transcript.DocumentaryTranscript;
import de.faustedition.transcript.Transcript;
import de.faustedition.transcript.Transcript.Type;

@GraphDatabaseTransactional
public class DocumentResource extends ServerResource {
	private final TemplateRepresentationFactory viewFactory;
	private Document document;

	@Inject
	public DocumentResource(TemplateRepresentationFactory viewFactory) {
		this.viewFactory = viewFactory;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	@Get("html")
	public Representation overview() throws IOException {
		Map<String, Object> viewModel = new HashMap<String, Object>();
		viewModel.put("document", document);
		viewModel.put("contents", document.getSortedContents());

		return viewFactory.create("document/document", getRequest().getClientInfo(), viewModel);
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

				final Transcript transcript = unit.getTranscript();
				if (transcript != null) {
					generator.writeObjectFieldStart("transcript");
					generator.writeStringField("type", transcript.getType().name().toLowerCase());
					generator.writeStringField("source", transcript.getSource().toString());
					if (transcript.getType() == Type.DOCUMENTARY) {
						DocumentaryTranscript dt = (DocumentaryTranscript) transcript;
						generator.writeArrayFieldStart("facsimiles");
						for (FaustURI facsimile : dt.getFacsimileReferences()) {
							generator.writeString(facsimile.toString());
						}
						generator.writeEndArray();
					}
					generator.writeEndObject();
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
