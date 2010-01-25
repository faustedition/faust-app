package de.faustedition.model.tei;

import static org.apache.jackrabbit.JcrConstants.NT_FILE;
import static org.apache.jackrabbit.JcrConstants.NT_FOLDER;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

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

import de.faustedition.model.repository.RepositoryDocument;
import de.faustedition.util.LoggingUtil;
import de.faustedition.util.XMLException;
import de.faustedition.util.XMLUtil;
import de.faustedition.web.dav.AbstractDavHandler;

@Service
public class EncodedTextDocumentDavHandler extends AbstractDavHandler {
	private static final Pattern UNWANTED_FILE_NAMES = Pattern.compile("^\\.");
	private static final String OXYGEN_SCHEMA_PI = "oxygen";
	private static final String XML_STYLESHEET_PI = "xml-stylesheet";
	private static final String CSS_STYLE_SHEET_PATH = "schema/faust.css";
	private static final String RNG_SCHEMA_PATH = "schema/faust.rnc";

	@Autowired
	private EncodedTextDocumentManager documentManager;

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
			RepositoryDocument repositoryDoc = new RepositoryDocument((Node) context.getExportRoot());
			EncodedTextDocument document = repositoryDoc.getDocument();

			documentManager.process(document);
			Document xml = document.getDocument();
			String cssStylesheetUri = XmlUtil.escape(getBaseURI() + CSS_STYLE_SHEET_PATH);
			String styleData = String.format("href=\"%s\" type=\"text/css\"", cssStylesheetUri);
			xml.insertBefore(xml.createProcessingInstruction(XML_STYLESHEET_PI, styleData), xml.getDocumentElement());

			String rngSchemaUri = XmlUtil.escape(getBaseURI() + RNG_SCHEMA_PATH);
			String schemaData = String.format("RNGSchema=\"%s\" type=\"compact\"", rngSchemaUri);
			xml.insertBefore(xml.createProcessingInstruction(OXYGEN_SCHEMA_PI, schemaData), xml.getDocumentElement());

			byte[] documentData = XMLUtil.serialize(document.getDocument(), true);
			int contentLength = documentData.length;
			long lastModified = repositoryDoc.getLastModified().getTime();

			if (context.hasStream()) {
				IOUtils.write(documentData, context.getOutputStream());
			}
			context.setContentType(repositoryDoc.getMimeType(), repositoryDoc.getEncoding());
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
		String name = context.getSystemId();
		if (UNWANTED_FILE_NAMES.matcher(name).matches()) {
			LoggingUtil.LOG.debug("Silently discarding request to import unwanted file '" + name + "'");
			context.informCompleted(true);
			return true;
		}

		InputStream in = null;
		try {
			EncodedTextDocument document = EncodedTextDocument.parse(in = context.getInputStream());

			Document xml = document.getDocument();
			for (org.w3c.dom.Node piNode : document.xpath("/processing-instruction()")) {
				ProcessingInstruction pi = (ProcessingInstruction) piNode;
				if (XML_STYLESHEET_PI.equals(pi.getTarget()) || OXYGEN_SCHEMA_PI.equals(pi.getTarget())) {
					xml.removeChild(pi);
				}
			}

			documentManager.process(document);

			Node parentNode = (Node) context.getImportRoot();
			RepositoryDocument repoDocument = null;
			if (parentNode.hasNode(name)) {
				repoDocument = new RepositoryDocument(parentNode.getNode(name));
				repoDocument.setDocument(document);
			} else {
				repoDocument = RepositoryDocument.create(parentNode, name, document);
			}
			return true;
		} catch (XMLException e) {
		} finally {
			IOUtils.closeQuietly(in);
		}
		return false;
	}
}
