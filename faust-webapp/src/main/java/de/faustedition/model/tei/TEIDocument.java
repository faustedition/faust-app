package de.faustedition.model.tei;

import java.io.IOException;
import java.util.List;

import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.xpath.XPath;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.rng.CompactSchemaReader;

import de.faustedition.util.ErrorUtil;
import de.faustedition.util.XMLUtil;

public class TEIDocument
{
	public static final Resource RELAX_NG_SCHEMA_RESOURCE = new ClassPathResource("/schema/faust-tei.rnc");
	public static final String TEI_NS_URI = "http://www.tei-c.org/ns/1.0";
	public static final String TEI_SIG_GE_URI = "http://www.tei-c.org/ns/geneticEditions";
	public static final String SVG_NS_URI = "http://www.w3.org/2000/svg";
	private static Schema schema;

	private Node node;

	public TEIDocument()
	{
		this(XMLUtil.documentBuilder().newDocument());
		node.appendChild(getDocument().createElementNS(TEI_NS_URI, "TEI"));
	}

	public TEIDocument(Node node)
	{
		this.node = node;
	}

	public Document getDocument()
	{
		return XMLUtil.getDocument(node);
	}

	public Element getTextElement()
	{
		return Preconditions.checkNotNull(findElementByPath("text"));
	}

	public Element getRevisionElement()
	{
		return Preconditions.checkNotNull(findElementByPath("teiHeader", "revisionDesc"));
	}

	public static XPath xpath()
	{
		SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
		namespaceContext.bindDefaultNamespaceUri(TEI_NS_URI);
		namespaceContext.bindNamespaceUri("svg", SVG_NS_URI);
		namespaceContext.bindNamespaceUri("ge", TEI_SIG_GE_URI);
		
		XPath xpath = XMLUtil.xpath();
		xpath.setNamespaceContext(namespaceContext);
		return xpath;
	}

	public Element findElementByPath(String... pathComponents)
	{
		Element element = getDocument().getDocumentElement();

		for (String localName : pathComponents)
		{
			element = XMLUtil.getChild(element, localName);
			if (element == null)
			{
				break;
			}
		}

		return element;
	}

	public static boolean isValid(Document document)
	{
		return validate(document).isEmpty();
	}

	public static List<SAXParseException> validate(Document document)
	{
		final List<SAXParseException> errors = Lists.newLinkedList();
		PropertyMapBuilder propertyMapBuilder = new PropertyMapBuilder();
		propertyMapBuilder.put(ValidateProperty.ERROR_HANDLER, new ErrorHandler()
		{
			@Override
			public void error(SAXParseException exception) throws SAXException
			{
				errors.add(exception);
			}

			@Override
			public void fatalError(SAXParseException exception) throws SAXException
			{
				errors.add(exception);
			}

			@Override
			public void warning(SAXParseException exception) throws SAXException
			{
				errors.add(exception);
			}
		});

		Validator validator = schema.createValidator(propertyMapBuilder.toPropertyMap());
		try
		{
			XMLUtil.nullTransformer(false).transform(new DOMSource(document), new SAXResult(validator.getContentHandler()));
			return errors;
		}
		catch (TransformerException e)
		{
			throw ErrorUtil.fatal(e, "XSLT error while validating TEI document");
		}
	}

	static
	{
		try
		{
			schema = CompactSchemaReader.getInstance().createSchema(new InputSource(RELAX_NG_SCHEMA_RESOURCE.getInputStream()), new PropertyMapBuilder().toPropertyMap());
		}
		catch (IOException e)
		{
			throw ErrorUtil.fatal(e, "I/O error while compiling RELAX NG schema");
		}
		catch (SAXException e)
		{
			throw ErrorUtil.fatal(e, "XML error while compiling RELAX NG schema");
		}
		catch (IncorrectSchemaException e)
		{
			throw ErrorUtil.fatal(e, "Schema error while compiling RELAX NG schema");
		}
	}
}
