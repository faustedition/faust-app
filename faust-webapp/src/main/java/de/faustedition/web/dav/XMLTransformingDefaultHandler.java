package de.faustedition.web.dav;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;

import de.faustedition.util.XMLUtil;

public class XMLTransformingDefaultHandler {

	public void transform(File dataFile, OutputStream stream) throws IOException {
		FileInputStream dataFileStream = null;
		try {
			IOUtils.copy((dataFileStream = new FileInputStream(dataFile)), stream);
		} finally {
			IOUtils.closeQuietly(dataFileStream);
			dataFile.delete();
		}

	}

	public File transform(String path, InputStream inputStream) throws IOException {
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
