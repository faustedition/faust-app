package de.faustedition.model.tei;

import static org.apache.jackrabbit.JcrConstants.NT_FILE;
import static org.apache.jackrabbit.JcrConstants.NT_FOLDER;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import net.sf.practicalxml.XmlUtil;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.server.io.ExportContext;
import org.apache.jackrabbit.server.io.ImportContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.ProcessingInstruction;

import de.faustedition.model.repository.RepositoryFile;
import de.faustedition.util.XMLException;
import de.faustedition.util.XMLUtil;
import de.faustedition.web.dav.AbstractDavHandler;

@Service
public class EncodedDocumentDavHandler extends AbstractDavHandler {
	private static final String OXYGEN_SCHEMA_PI = "oxygen";
	private static final String XML_STYLESHEET_PI = "xml-stylesheet";
	private static final String CSS_STYLE_SHEET_PATH = "schema/faust.css";
	private static final String RNG_SCHEMA_PATH = "schema/faust.rnc";

	@Autowired
	private EncodedDocumentManager teiDocumentManager;

	@Override
	public boolean canExport(ExportContext context, boolean isCollection) throws RepositoryException {
		return isOfNonCollectionNodeType(NT_FILE, context.getExportRoot(), isCollection);
	}

	@Override
	public boolean canImport(ImportContext context, boolean isCollection) throws RepositoryException {
		if (isCollection) {
			return false;
		}

		Item contextItem = context.getImportRoot();
		String name = context.getSystemId();
		if (contextItem == null || name == null) {
			return false;
		}

		if (!contextItem.isNode()) {
			return false;
		}
		Node node = (Node) contextItem;
		return (node.hasNode(name) ? node.getNode(name).isNodeType(NT_FILE) : node.isNodeType(NT_FOLDER));
	}

	@Override
	public boolean exportContent(ExportContext context, boolean isCollection) throws RepositoryException {
		try {
			RepositoryFile repositoryFile = new RepositoryFile((Node) context.getExportRoot());
			EncodedDocument teiDocument = new EncodedDocumentWrapper(repositoryFile).parse();

			teiDocumentManager.process(teiDocument);
			Document d = teiDocument.getDocument();
			String cssStylesheetUri = XmlUtil.escape(getBaseURI() + CSS_STYLE_SHEET_PATH);
			String styleData = String.format("href=\"%s\" type=\"text/css\"", cssStylesheetUri);
			d.insertBefore(d.createProcessingInstruction(XML_STYLESHEET_PI, styleData), d.getDocumentElement());

			String rngSchemaUri = XmlUtil.escape(getBaseURI() + RNG_SCHEMA_PATH);
			String schemaData = String.format("RNGSchema=\"%s\" type=\"compact\"", rngSchemaUri);
			d.insertBefore(d.createProcessingInstruction(OXYGEN_SCHEMA_PI, schemaData), d.getDocumentElement());

			byte[] documentData = XMLUtil.serialize(teiDocument.getDocument(), true);
			int contentLength = documentData.length;
			long lastModified = repositoryFile.getLastModified().getTime();

			if (context.hasStream()) {
				IOUtils.write(documentData, context.getOutputStream());
			}
			context.setContentType("application/xml", "UTF-8");
			context.setContentLength(contentLength);
			context.setModificationTime(lastModified);
			context.setETag(String.format("%s-%s", contentLength, lastModified));
			return true;
		} catch (XMLException e) {
		} catch (IOException e) {
		}
		return false;
	}

	@Override
	public boolean importContent(ImportContext context, boolean isCollection) throws RepositoryException {
		if (!context.hasStream()) {
			return false;
		}
		InputStream in = null;
		try {
			EncodedDocument teiDocument = EncodedDocument.parse(in = context.getInputStream());
			Document d = teiDocument.getDocument();
			for (org.w3c.dom.Node piNode : teiDocument.xpath("/processing-instruction()")) {
				ProcessingInstruction pi = (ProcessingInstruction) piNode;
				if (XML_STYLESHEET_PI.equals(pi.getTarget()) || OXYGEN_SCHEMA_PI.equals(pi.getTarget())) {
					d.removeChild(pi);
				}
			}
			teiDocumentManager.process(teiDocument);
			ByteArrayInputStream documentStream = new ByteArrayInputStream(XMLUtil.serialize(d, false));
			Node parentNode = (Node) context.getImportRoot();
			String name = context.getSystemId();

			RepositoryFile file = null;
			if (parentNode.hasNode(name)) {
				file = new RepositoryFile(parentNode.getNode(name));
				file.setContent(documentStream);
			} else {
				file = RepositoryFile.create(parentNode, name, documentStream);
			}
			file.setMimeType("application/xml");
			file.setEncoding("UTF-8");
			return true;
		} catch (XMLException e) {
		} finally {
			IOUtils.closeQuietly(in);
		}
		return false;
	}
}
