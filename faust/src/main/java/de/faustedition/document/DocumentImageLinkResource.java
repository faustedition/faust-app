package de.faustedition.document;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.goddag4j.visit.GoddagVisitor;
import org.restlet.representation.CharacterRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.restlet.data.MediaType;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.ext.xml.XmlRepresentation;
import org.xml.sax.InputSource;

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
	private int pageNum;
	private final String imageUrlTemplate;
	private Logger logger;

	@Inject
	public DocumentImageLinkResource(TemplateRepresentationFactory viewFactory,
			@Named("facsimile.iip.url") String imageServerUrl, Logger logger) {
		this.viewFactory = viewFactory;
		this.logger = logger;
		this.imageUrlTemplate = imageServerUrl + "?FIF=%s.tif&SDS=0,90&CNT=1.0&WID=800&QLT=90&CVT=jpeg";
	}

	public void setDocument(Document document, int page) {
		this.document = document;
		this.pageNum = page;
	}

	@Get("html")
	public Representation overview() throws IOException {
		Map<String, Object> viewModel = new HashMap<String, Object>();
		viewModel.put("document", document);
		viewModel.put("contents", document.getSortedContents());
		Object[] contents = document.getSortedContents().toArray();
		if (contents.length < pageNum) {
			logger.log(Level.WARNING, "Request for page " + pageNum + ", but there are only " + contents.length + " pages.");
			return null;
		}
		MaterialUnit mu = (MaterialUnit)contents[pageNum];
		viewModel.put("pageNum", pageNum);

		final Transcript transcript = mu.getTranscript();
		if (transcript == null) {
			return null;
		}
		if (transcript.getType() != Type.DOCUMENTARY) {
			return null;
		}
		final DocumentaryTranscript dt = (DocumentaryTranscript) transcript;
		if (dt.getFacsimileReferences().isEmpty()) {
			return null;
		}
		final FaustURI facsimileURI = dt.getFacsimileReferences().first();


		String facsimileUrl = URLEncoder.encode(String.format(
				imageUrlTemplate, facsimileURI.getPath().replaceAll("^/", "")), "UTF-8");
		
		viewModel.put("facsimileUrl", facsimileUrl);


		return viewFactory.create("document/imagelink", getRequest().getClientInfo(), viewModel);


	}

	@Get("svg")
	public Representation graphic() {

		return new WriterRepresentation(MediaType.IMAGE_SVG) {

			@Override
			public void write(Writer writer) throws IOException {
				writer.write("<svg width=\"640\" height=\"480\" xmlns=\"http://www.w3.org/2000/svg\">");
				writer.write(" <g>");
				writer.write("  <title>Layer 1</title>");
				writer.write("  <rect id=\"svg_2\" height=\"118\" width=\"170\" y=\"141\" x=\"163\" fill-opacity=\"0.5\" stroke-opacity=\"0.5\" stroke=\"#000000\" fill=\"#FFFF00\"/>");
				writer.write(" </g>");
				writer.write("</svg>");

			}
		};

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
