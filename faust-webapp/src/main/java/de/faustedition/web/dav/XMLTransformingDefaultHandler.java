package de.faustedition.web.dav;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.server.io.DefaultHandler;
import org.apache.jackrabbit.server.io.ExportContext;
import org.apache.jackrabbit.server.io.IOManager;
import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.server.io.ImportContext;

import de.faustedition.util.XMLUtil;

public class XMLTransformingDefaultHandler extends DefaultHandler {

	public XMLTransformingDefaultHandler() {
		super();
	}

	public XMLTransformingDefaultHandler(IOManager ioManager, String collectionNodetype, String defaultNodetype, String contentNodetype) {
		super(ioManager, collectionNodetype, defaultNodetype, contentNodetype);
	}

	public XMLTransformingDefaultHandler(IOManager ioManager) {
		super(ioManager);
	}

	@Override
	protected boolean importData(ImportContext context, boolean isCollection, Node contentNode) throws IOException, RepositoryException {
		InputStream in = context.getInputStream();
		if (in != null) {
			// NOTE: with the default folder-nodetype (nt:folder) no
			// inputstream
			// is allowed. setting the property would therefore
			// fail.
			if (isCollection) {
				return false;
			}

			File dataFile = transform(contentNode.getPath(), in);
			FileInputStream dataFileStream = null;
			try {
				contentNode.setProperty(JcrConstants.JCR_DATA, (dataFileStream = new FileInputStream(dataFile)));
			} finally {
				IOUtils.closeQuietly(dataFileStream);
				dataFile.delete();
			}
		}
		// success if no data to import.
		return true;
	}

	@Override
	protected void exportData(ExportContext context, boolean isCollection, Node contentNode) throws IOException, RepositoryException {
		if (contentNode.hasProperty(JcrConstants.JCR_DATA)) {
			Property p = contentNode.getProperty(JcrConstants.JCR_DATA);
			File dataFile = transform(contentNode.getPath(), p.getStream());
			FileInputStream dataFileStream = null;
			try {
				IOUtil.spool((dataFileStream = new FileInputStream(dataFile)), context.getOutputStream());
			} finally {
				IOUtils.closeQuietly(dataFileStream);
				dataFile.delete();
			}
		}
	}

	private File transform(String path, InputStream inputStream) throws IOException {
		File bufferFile = createBufferFile(inputStream);
		try {
			return transform(bufferFile);
		} catch (TransformerException e) {
			return bufferFile;
		}
	}

	private File createBufferFile(InputStream inputStream) throws IOException {
		File bufferFile = File.createTempFile(getClass().getName() + "_transform_input", ".xml");
		FileOutputStream bufferFileStream = null;

		try {
			bufferFile.deleteOnExit();
			IOUtils.copy(inputStream, (bufferFileStream = new FileOutputStream(bufferFile)));
		} finally {
			IOUtils.closeQuietly(bufferFileStream);
			IOUtils.closeQuietly(inputStream);
		}

		return bufferFile;
	}

	private File transform(File file) throws IOException, TransformerException {
		File resultFile = File.createTempFile(getClass().getName() + "_format_output", ".xml");
		OutputStreamWriter tempWriter = null;
		FileInputStream fileInputStream = null;
		TransformerException transformerException = null;

		try {
			resultFile.deleteOnExit();
			fileInputStream = new FileInputStream(file);
			tempWriter = new OutputStreamWriter(new FileOutputStream(resultFile), "utf-8");
			XMLUtil.serializingTransformer(true).transform(new StreamSource(fileInputStream), new StreamResult(tempWriter));
			IOUtils.closeQuietly(tempWriter);
			IOUtils.closeQuietly(fileInputStream);
		} catch (TransformerException e) {
			transformerException = e;
		} finally {
			IOUtils.closeQuietly(tempWriter);
			IOUtils.closeQuietly(fileInputStream);

		}

		if (transformerException == null) {
			file.delete();
			return resultFile;
		} else {
			resultFile.delete();
			throw transformerException;
		}

	}
}
