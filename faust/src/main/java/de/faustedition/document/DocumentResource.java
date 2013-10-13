package de.faustedition.document;

import de.faustedition.Templates;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;


@Path("/document")
@Singleton
public class DocumentResource {

    private final Templates templates;

    @Inject
    public DocumentResource(Templates templates) {
        this.templates = templates;
    }


	@GET
    @Produces(MediaType.TEXT_HTML)
	public Response overview() throws IOException {
        /*
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
		*/
        return null;
	}

    @GET
    @Produces(DocumentImageLinks.IMAGE_SVG_TYPE)
	public Response graphic() {
		String result = "<xml version=\"1open.0\"?><svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">"
			+ "<script type=\"text/javascript\" ></script>"
			+ "</svg>";
		
		/*
		return new StringRepresentation(result, MediaType.IMAGE_SVG,
				Language.DEFAULT, CharacterSet.UTF_8);
        */
        return Response.ok(result).build();
	}

    @GET
    @Produces(MediaType.APPLICATION_JSON)
	public Response documentStructure() {
        /*
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
		*/
        return null;
	}

	
}
