package de.faustedition.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.practicalxml.XmlException;

import org.w3c.dom.Document;

public class XMLUtil {
	public static Transformer nullTransformer(boolean indent) throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setErrorListener(new StrictNoOutputErrorListener());
		if (indent) {
			transformerFactory.setAttribute("indent-number", 4);
		}

		Transformer transformer = transformerFactory.newTransformer();
		transformer.setErrorListener(new StrictNoOutputErrorListener());
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		if (indent) {
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		}
		return transformer;
	}

	public static byte[] serialize(Document document, boolean indent) {
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			serialize(document, new OutputStreamWriter(byteStream, "UTF-8"), indent);
			return byteStream.toByteArray();
		} catch (IOException e) {
			throw new XmlException("I/O error while serializing document", e);
		}
	}

	public static void serialize(Document document, OutputStream stream, boolean indent) {
		try {
			serialize(document, new OutputStreamWriter(stream, "UTF-8"), indent);
		} catch (IOException e) {
			throw new XmlException("I/O error while serializing document", e);
		}
	}

	public static void serialize(Document document, Writer writer, boolean indent) {
		try {
			nullTransformer(indent).transform(new DOMSource(document), new StreamResult(writer));
		} catch (TransformerException e) {
			throw new XmlException("XSLT error while serializing document", e);
		}
	}

	private static class StrictNoOutputErrorListener implements ErrorListener {

		@Override
		public void error(TransformerException exception) throws TransformerException {
			throw exception;
		}

		@Override
		public void fatalError(TransformerException exception) throws TransformerException {
			throw exception;
		}

		@Override
		public void warning(TransformerException exception) throws TransformerException {
			throw exception;
		}

	}
}
