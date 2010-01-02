package de.faustedition.model.dav;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import net.sf.practicalxml.ParseUtil;
import net.sf.practicalxml.XmlUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.InputSource;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

import de.faustedition.model.document.TranscriptionDocument;
import de.faustedition.model.tei.TEIDocument;
import de.faustedition.util.XMLUtil;

public class TranscriptionDavResource extends DavResourceBase implements GetableResource, PropFindableResource {
	private static final String OXYGEN_SCHEMA_PI = "oxygen";
	private static final String XML_STYLESHEET_PI = "xml-stylesheet";
	public static final String RESOURCE_NAME_SUFFIX = "_transcription.xml";
	private static final String CSS_STYLE_SHEET_PATH = "/schema/faust.css";
	private static final String RNG_SCHEMA_PATH = "/schema/faust.rnc";

	private final TranscriptionDocument transcriptionDocument;
	private byte[] documentData;

	protected TranscriptionDavResource(DavResourceFactory factory, TranscriptionDocument transcriptionDocument) {
		super(factory);
		this.transcriptionDocument = transcriptionDocument;
	}

	@Override
	public String getName() {
		return transcriptionDocument.getFacettedNode().getName() + RESOURCE_NAME_SUFFIX;
	}

	@Override
	public Date getCreateDate() {
		return transcriptionDocument.getCreated();
	}

	@Override
	public Date getModifiedDate() {
		return transcriptionDocument.getLastModified();
	}

	protected byte[] documentData() {
		if (documentData == null) {
			Document d = transcriptionDocument.getTeiDocument().getDocument();

			String cssStylesheetUri = XmlUtil.escape(factory.getBaseURI() + CSS_STYLE_SHEET_PATH);
			String styleData = String.format("href=\"%s\" type=\"text/css\"", cssStylesheetUri);
			d.insertBefore(d.createProcessingInstruction(XML_STYLESHEET_PI, styleData), d.getDocumentElement());

			String rngSchemaUri = XmlUtil.escape(factory.getBaseURI() + RNG_SCHEMA_PATH);
			String schemaData = String.format("RNGSchema=\"%s\" type=\"compact\"", rngSchemaUri);
			d.insertBefore(d.createProcessingInstruction(OXYGEN_SCHEMA_PI, schemaData), d.getDocumentElement());
			documentData = XMLUtil.serialize(d, true);
		}
		return documentData;
	}

	@Override
	public Long getContentLength() {
		return Long.valueOf(documentData().length);
	}

	@Override
	public String getContentType(String accepts) {
		return "application/xml";
	}

	@Override
	public Long getMaxAgeSeconds(Auth auth) {
		return null;
	}

	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException,
			NotAuthorizedException {
		out.write(documentData());
		out.flush();
	}

	@Override
	public Object getLockResource() {
		return transcriptionDocument;
	}

	public void update(InputStream inputStream) throws IOException {
		Document d = ParseUtil.parse(new InputSource(inputStream));
		for (Node child : XMLUtil.iterableNodeList(d.getChildNodes())) {
			if (child.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
				ProcessingInstruction pi = (ProcessingInstruction) child;
				if (XML_STYLESHEET_PI.equals(pi.getNodeName()) || OXYGEN_SCHEMA_PI.equals(pi.getNodeName())) {
					d.removeChild(child);
				}
			}
		}
		transcriptionDocument.setDocumentData(new TEIDocument(d));
		transcriptionDocument.setLastModified(new Date());
	}
}
