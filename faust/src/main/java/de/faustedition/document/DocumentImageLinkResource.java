package de.faustedition.document;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.restlet.data.MediaType;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.document.XMLDocumentImageLinker.IdGenerator;
import de.faustedition.graph.GraphDatabaseTransactional;
import de.faustedition.template.TemplateRepresentationFactory;
import de.faustedition.transcript.DocumentaryTranscript;
import de.faustedition.transcript.Transcript;
import de.faustedition.transcript.Transcript.Type;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;
import de.faustedition.xml.XPathUtil;

@GraphDatabaseTransactional
public class DocumentImageLinkResource extends ServerResource {
	private final TemplateRepresentationFactory viewFactory;
	private Document document;
	private int pageNum;
	private final String imageUrlTemplate;
	private final Logger logger;
	private final IIPInfo iipInfo;
	private XMLStorage xml;
	
	private static String lineXP = "//ge:line";
	
	
	public class NullOutputStream extends OutputStream {
		  @Override
		  public void write(int b) throws IOException {
		  }
		}

	
	private class LineIdGenerator extends XMLDocumentImageLinker.IdGenerator {

		private int index = 0;

		@Override
		public String next() {
			return String.format("lineNumber%d", index++);
		}		
	}
	
	@Inject
	public DocumentImageLinkResource(XMLStorage xml,
			TemplateRepresentationFactory viewFactory,
			@Named("facsimile.iip.url") String imageServerUrl, IIPInfo iipInfo,
			Logger logger) {
		this.xml = xml;
		this.viewFactory = viewFactory;
		this.logger = logger;
		this.imageUrlTemplate = imageServerUrl + "?FIF=%s.tif";
		this.iipInfo = iipInfo;
	}

	public void setDocument(Document document, int page) {
		this.document = document;
		this.pageNum = page;
	}

	private void writeSVGImageLinks(final org.w3c.dom.Document svg,
			URI linkDataURI) throws TransformerException {
		FaustURI linkDataFaustURI = new FaustURI(linkDataURI);
		logger.log(Level.FINE, "Writing image-text-link data to "
				+ linkDataFaustURI);
		xml.put(linkDataFaustURI, svg);
	}
	
	@Put("svg")
	public String store(InputRepresentation data) throws SAXException,
			IOException, TransformerException, XPathExpressionException,
			URISyntaxException {
		final org.w3c.dom.Document svg = XMLUtil.parse(data.getStream());
		final FaustURI transcriptURI = transcript().getSource();
		final org.w3c.dom.Document source = XMLUtil.parse(xml
				.getInputSource(transcriptURI));
		
		IdGenerator newIds = new IdGenerator() {
			
			/**
			 * Maps a number to a string of lowercase alphabetic characters, 
			 * which act as digits to base 26. (a, b, c, ..., aa, ab, ac, ...) 
			 * @param n 
			 * @return 
			 */
			private String alphabetify(int n) {
				if (n > 25) 
					return  alphabetify(n / 26 - 1) + alphabetify(n % 26);
				else
					return "" + (char)(n + 97);
					
			}
			
			
			private int index = 0;

			@Override
			public String next() {
				return String.format("l%s", alphabetify(index++));
			}
		};
		
		javax.xml.xpath.XPathExpression lines = XPathUtil.xpath(this.lineXP);
		
		boolean hasSourceChanged = XMLDocumentImageLinker.link(source, new LineIdGenerator(), lines, svg, newIds);
		if (hasSourceChanged)
			logger.log(Level.FINE, "Added new xml:ids to "
					+ transcriptURI);
			
			
		// Check if the transcript has image links attached
		URI linkDataURI = XMLDocumentImageLinker.linkDataURI(source);
		
		if (linkDataURI != null) {
			// we already have a file, do nothing

		} else {
			logger.log(Level.FINE, "Adding new image-text-link to "
					+ transcriptURI);

			// generate random URI
			
			String uuid = UUID.randomUUID().toString();
			linkDataURI = new URI(FaustURI.FAUST_SCHEME, FaustAuthority.XML
					.name().toLowerCase(),
					"/image-text-links/" + uuid + ".svg", null);


			XMLDocumentImageLinker.insertLinkURI(source, linkDataURI);
			hasSourceChanged = true;
		}
		
		// write the image links file		
		writeSVGImageLinks(svg, linkDataURI);		
		
		if (hasSourceChanged) {
			// write the modified transcript
			logger.log(Level.FINE, "Writing "
				+ transcriptURI);
			xml.put(transcriptURI, source);
		}


		// FIXME
		return "<svg></svg>";
	}


	@Get("html")
	public Representation overview() throws IOException {
		final Map<String, Object> viewModel = new HashMap<String, Object>();
		viewModel.put("document", document);
		viewModel.put("pageNum", pageNum);

		final String facsimileUrl = URLEncoder.encode(facsimileUrl()
				+ "&SDS=0,90&CNT=1.0&WID=800&QLT=90&CVT=jpeg", "UTF-8");
		viewModel.put("facsimileUrl", facsimileUrl);

		return viewFactory.create("document/imagelink", getRequest()
				.getClientInfo(), viewModel);
	}

	protected MaterialUnit page() {

		final Object[] contents = document.getSortedContents().toArray();
		if (contents.length < pageNum) {
			logger.log(Level.WARNING, "Request for page " + pageNum
					+ ", but there are only " + contents.length + " pages.");
			return null;
		}

		return (MaterialUnit) contents[pageNum];
	}

	private DocumentaryTranscript transcript() {

		final MaterialUnit mu = page();
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
		return dt;
	}

	protected String facsimileUrl() {
		final DocumentaryTranscript dt = transcript();
		final FaustURI facsimileURI = dt.getFacsimileReferences().first();
		return String.format(imageUrlTemplate, facsimileURI.getPath()
				.replaceAll("^/", ""));
	}

	@Get("svg")
	public Representation graphic() throws ResourceException, IOException,
			SAXException, XPathExpressionException, URISyntaxException {

		iipInfo.retrieve(facsimileUrl());
		final int width = iipInfo.getWidth();
		final int height = iipInfo.getHeight();
		//final int width = 4000;
		//final int height = 6000;

		final FaustURI transcriptURI = transcript().getSource();
		final org.w3c.dom.Document source = XMLUtil.parse(xml
				.getInputSource(transcriptURI));

		// Check if the transcript has image links attached
		final URI linkDataURI = XMLDocumentImageLinker.linkDataURI(source);
		if (linkDataURI != null) {
			logger.log(Level.FINE, transcriptURI
					+ " has image-text links, loading");

			org.w3c.dom.Document svg = XMLUtil.parse(xml
					.getInputSource(new FaustURI(linkDataURI)));
			
			// adjust the links in the xp
			final XPathExpression xp = XPathUtil.xpath(this.lineXP);

			XMLDocumentImageLinker.enumerateTarget(source, svg, xp, new LineIdGenerator(), new NullOutputStream());				
			
			// FIXME what if the image size changes? Change the size of the SVG? 
			// Scale it? Leave it alone?
			return new DomRepresentation(MediaType.IMAGE_SVG, svg);
			// return new SaxRepresentation(MediaType.IMAGE_SVG,
			// XMLUtil.parse(xml.getInputSource(linkDataURI)));
			

		} else {
			logger.log(Level.FINE, transcriptURI
					+ " doesn't have image-text links yet");
			return new WriterRepresentation(MediaType.IMAGE_SVG) {

				@Override
				public void write(Writer writer) throws IOException {
					writer.write("<svg width=\"" + width + "\" height=\""
							+ height
							+ "\" xmlns=\"http://www.w3.org/2000/svg\">");
					writer.write(" <g>");
					writer.write("  <title>Layer 1</title>");
					writer.write(" </g>");
					writer.write("</svg>");
				}
			};
		}
	}

	@Get("json")
	public Representation documentStructure() throws SAXException, IOException,
			XPathExpressionException {
		final FaustURI transcriptURI = transcript().getSource();
		final org.w3c.dom.Document source = XMLUtil.parse(xml
				.getInputSource(transcriptURI));
		final XPathExpression xp = XPathUtil.xpath(lineXP);
		
		return new OutputRepresentation(MediaType.APPLICATION_JSON) {
			@Override
			public void write(OutputStream outputStream) throws IOException {
				XMLDocumentImageLinker.enumerateTarget(source, null, xp, new LineIdGenerator(), outputStream);				
			}
		};
	}
	
//	@Put("json")
//	public String storeStructure(InputRepresentation data) throws JsonParseException, IOException {
//		
//		
//		
//		JsonParser parser = new JsonFactory().createJsonParser(data.getStream());
//		
//		
//		parser.nextToken(); //Start object
//		while (parser.nextToken() != JsonToken.END_OBJECT) {
//			String rootFieldname = parser.getCurrentName();
//			if (rootFieldname != "lines")
//				throw new IllegalArgumentException("Not a valid document content description!");
//			parser.nextToken(); //Start object
//			while (parser.nextToken() != JsonToken.END_OBJECT) {
//				String namefield = parser.getCurrentName();
//				parser.nextToken(); //move to value
//				if ("id".equals(namefield)) {
//				} else if ("".equals(namefield)) {
//					
//				} else if ("".equals(namefield)) {
//					
//				}
//			}
//		}
//		parser.close();
//		
//		
//		//FIXME
//		return "{}";
//	}
	


}
