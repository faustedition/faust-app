package de.faustedition.document;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.goddag4j.visit.GoddagVisitor;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.faustedition.FaustURI;
import de.faustedition.JsonRespresentation;
import de.faustedition.graph.GraphDatabaseTransactional;
import de.faustedition.template.TemplateRepresentationFactory;
import de.faustedition.transcript.DocumentaryTranscript;
import de.faustedition.transcript.Transcript;
import de.faustedition.transcript.Transcript.Type;

@GraphDatabaseTransactional
public class DocumentImageLinkResource extends ServerResource {
	private final TemplateRepresentationFactory viewFactory;
	private Document document;
	private final String imageUrlTemplate;

	@Inject
	public DocumentImageLinkResource(TemplateRepresentationFactory viewFactory,
			@Named("facsimile.iip.url") String imageServerUrl) {
		this.viewFactory = viewFactory;
		this.imageUrlTemplate = imageServerUrl + "?FIF=%s.tif&SDS=0,90&CNT=1.0&WID=800&QLT=90&CVT=jpeg";
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	@Get("html")
	public Representation overview() throws IOException {
		Map<String, Object> viewModel = new HashMap<String, Object>();
		viewModel.put("document", document);
		viewModel.put("contents", document.getSortedContents());

		return viewFactory.create("document/imagelink", getRequest().getClientInfo(), viewModel);
	}

	@Get("json")
	public Representation documentStructure() {
		return new JsonRespresentation() {

			@Override
			protected void generate() throws IOException {
				generator.writeStartObject();
				generator.writeStringField("sourceFile", "");
				generator.writeArrayFieldStart("pages");
				
				int pageCount = 0;
				for (MaterialUnit mu : document.getSortedContents()) {
					final Transcript transcript = mu.getTranscript();
					if (transcript == null) {
						continue;
					}
					if (transcript.getType() != Type.DOCUMENTARY) {
						continue;
					}
					final DocumentaryTranscript dt = (DocumentaryTranscript) transcript;
					if (dt.getFacsimileReferences().isEmpty()) {
						continue;
					}
					generator.writeStartObject();
					final FaustURI uri = dt.getFacsimileReferences().first();
					generator.writeStringField("url",
							String.format(imageUrlTemplate, URLEncoder.encode(uri.getPath().replaceAll("^/", ""), "UTF-8")));
					generator.writeStringField("info", "Faust");
					generator.writeArrayFieldStart("lines");
					
					final int currentPage = pageCount++; 
					new GoddagVisitor() {
						private int lineCount = 0;
						private StringBuilder lineContent;
						
						public void startElement(org.goddag4j.Element root, org.goddag4j.Element element) {
							if ("line".equals(element.getName())) {
								lineContent = new StringBuilder();
							}
						}
						
						public void endElement(org.goddag4j.Element root, org.goddag4j.Element element) {
							if ("line".equals(element.getName())) {
								String line = lineContent.toString().trim();
								if (lineContent.length() > 0) {
									try {
										generator.writeStartObject();
										generator.writeStringField("id", "id_" + currentPage + "_" + lineCount);
										generator.writeStringField("text", line);
										generator.writeStringField("info", "");
										generator.writeEndObject();
									} catch (IOException e) {
										throw Throwables.propagate(e);
									}
								}
								lineContent = null;
								lineCount++;
							}							
						}
						
						public void text(org.goddag4j.Element root, org.goddag4j.Text text) {
							if (lineContent != null) {
								lineContent.append(text.getText());
							}
						}
					}.visit(dt.getDefaultRoot(), dt.getDefaultRoot());
					generator.writeEndArray();
					generator.writeEndObject();
				}
				generator.writeEndArray();
				generator.writeEndObject();
			}
		};
	}
}
