package de.faustedition.util;

import java.io.ByteArrayInputStream;
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

import net.sf.practicalxml.ParseUtil;
import net.sf.practicalxml.XmlException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.faustedition.model.TEIDocument;

public class XMLUtil
{
	public static TransformerFactory saxonTransformerFactory()
	{
		return TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", XMLUtil.class.getClassLoader());
	}

	public static Transformer nullTransformer(boolean indent) throws TransformerException
	{
		TransformerFactory transformerFactory = saxonTransformerFactory();
		transformerFactory.setErrorListener(new StrictNoOutputErrorCallback());

		Transformer transformer = transformerFactory.newTransformer();
		transformer.setErrorListener(new StrictNoOutputErrorCallback());
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.INDENT, (indent ? "yes" : "no"));
		return transformer;
	}

	public static byte[] serialize(Document document, boolean indent)
	{
		try
		{
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			serialize(document, new OutputStreamWriter(byteStream, "UTF-8"), indent);
			return byteStream.toByteArray();
		}
		catch (IOException e)
		{
			throw new XmlException("I/O error while serializing document", e);
		}
	}

	public static void serialize(Document document, OutputStream stream, boolean indent)
	{
		try
		{
			serialize(document, new OutputStreamWriter(stream, "UTF-8"), indent);
		}
		catch (IOException e)
		{
			throw new XmlException("I/O error while serializing document", e);
		}
	}

	public static void serialize(Document document, Writer writer, boolean indent)
	{
		try
		{
			nullTransformer(indent).transform(new DOMSource(document), new StreamResult(writer));
		}
		catch (TransformerException e)
		{
			throw new XmlException("XSLT error while serializing document", e);
		}
	}

	public static Document parse(byte[] data)
	{
		return ParseUtil.parse(new InputSource(new ByteArrayInputStream(data)));
	}

	public static boolean hasText(Element element)
	{
		for (Node textNode : TEIDocument.xpath(".//text()").evaluate(element))
		{
			if (StringUtils.isNotBlank(textNode.getTextContent()))
			{
				return true;
			}
		}
		return false;
	}

	public static class StrictNoOutputErrorCallback implements ErrorListener, ErrorHandler
	{

		@Override
		public void error(TransformerException exception) throws TransformerException
		{
			throw exception;
		}

		@Override
		public void fatalError(TransformerException exception) throws TransformerException
		{
			throw exception;
		}

		@Override
		public void warning(TransformerException exception) throws TransformerException
		{
			throw exception;
		}

		@Override
		public void error(SAXParseException exception) throws SAXException
		{
			throw exception;
			
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException
		{
			throw exception;
		}

		@Override
		public void warning(SAXParseException exception) throws SAXException
		{
			throw exception;			
		}

	}
}
