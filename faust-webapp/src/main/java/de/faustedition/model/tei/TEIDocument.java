package de.faustedition.model.tei;

import static net.sf.practicalxml.builder.XmlBuilder.attribute;
import static net.sf.practicalxml.builder.XmlBuilder.text;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.builder.ElementNode;
import net.sf.practicalxml.builder.Node;
import net.sf.practicalxml.builder.XmlBuilder;
import net.sf.practicalxml.xpath.XPathWrapper;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
	public static final String SVG_NS_URI = "http://www.w3.org/2000/svg";
	private static final Map<String, String> HAND_DECLARATIONS = new LinkedHashMap<String, String>();
	private static final Map<String, CharacterDeclaration> CHAR_DECLARATIONS = new LinkedHashMap<String, CharacterDeclaration>();

	private static Schema schema;

	private org.w3c.dom.Node node;

	public TEIDocument(org.w3c.dom.Node node)
	{
		this.node = node;
	}

	public Document getDocument()
	{
		return XMLUtil.getDocument(node);
	}

	public Element getTextElement()
	{
		Element textElement = DomUtil.getChild(getDocument().getDocumentElement(), "text");
		Preconditions.checkNotNull(textElement);
		return textElement;
	}

	public Element getRevisionElement()
	{
		Element revisionElement = DomUtil.getChild(DomUtil.getChild(getDocument().getDocumentElement(), "teiHeader"), "revisionDesc");
		Preconditions.checkNotNull(revisionElement);
		return revisionElement;
	}

	public boolean hasText()
	{
		return XMLUtil.hasText(getTextElement());
	}

	public void serialize(OutputStream stream, boolean indent)
	{
		XMLUtil.serialize(node, stream, indent);
	}

	public static ElementNode teiElementNode(String localName, Node... children)
	{
		return XmlBuilder.element(TEI_NS_URI, localName, children);
	}

	public static ElementNode teiRoot(Node... children)
	{
		ElementNode rootNode = teiElementNode("TEI", children);
		rootNode.addChild(XmlBuilder.attribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:svg", SVG_NS_URI));
		return rootNode;
	}

	public static XPathWrapper xpath(String expression)
	{
		XPathWrapper xPathWrapper = new XPathWrapper(expression);
		xPathWrapper.bindDefaultNamespace(TEI_NS_URI);
		xPathWrapper.bindNamespace("svg", SVG_NS_URI);
		return xPathWrapper;
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

	public static TEIDocument buildTemplate(String title)
	{
		ElementNode titleNode = teiElementNode("title", text(title));
		ElementNode fileDesc = teiElementNode("fileDesc", teiElementNode("titleStmt", titleNode), teiElementNode("publicationStmt", teiElementNode("p")), teiElementNode("sourceDesc",
				teiElementNode("p")));
		ElementNode encodingDesc = teiElementNode("encodingDesc", characterDeclarations());
		ElementNode profileDesc = teiElementNode("profileDesc", handDeclarations());

		Document document = DomUtil.newDocument();
		document.appendChild(document.importNode(TEIDocument.teiRoot(teiElementNode("teiHeader", fileDesc, encodingDesc, profileDesc)).toDOM().getDocumentElement(), true));
		
		return new TEIDocument(document);
	}

	private static Node characterDeclarations()
	{
		List<Node> declarations = new ArrayList<Node>(CHAR_DECLARATIONS.size());
		for (String id : CHAR_DECLARATIONS.keySet())
		{
			CharacterDeclaration declaration = CHAR_DECLARATIONS.get(id);

			ElementNode charNameNode = teiElementNode("charName", text(declaration.name));
			ElementNode descNode = teiElementNode("desc", text(declaration.description));
			ElementNode mappingNode = teiElementNode("mapping", attribute("type", "Unicode"), text(declaration.unicodeMapping));

			declarations.add(teiElementNode("char", attribute(XMLConstants.XML_NS_URI, "xml:id", id), charNameNode, descNode, mappingNode));
		}

		return teiElementNode("charDecl", declarations.toArray(new Node[declarations.size()]));
	}

	private static Node handDeclarations()
	{
		List<Node> declarations = new ArrayList<Node>(HAND_DECLARATIONS.size());
		for (String id : HAND_DECLARATIONS.keySet())
		{
			declarations.add(teiElementNode("handNote", attribute(XMLConstants.XML_NS_URI, "xml:id", id), text(HAND_DECLARATIONS.get(id))));
		}
		return teiElementNode("handNotes", declarations.toArray(new Node[declarations.size()]));
	}

	private static class CharacterDeclaration
	{
		private String name;
		private String description;
		private String unicodeMapping;

		private CharacterDeclaration(String name, String description, String unicodeMapping)
		{
			this.name = name;
			this.description = description;
			this.unicodeMapping = unicodeMapping;
		}
	}

	static
	{
		final Map<String, String> typefaces = new LinkedHashMap<String, String>();
		typefaces.put("lat", "latin");
		typefaces.put("gr", "greek");

		final Map<String, String> materials = new HashMap<String, String>();
		materials.put("t", "ink");
		materials.put("tr", "ink red/brown");
		materials.put("bl", "pencil");
		materials.put("ro", "ruddle");
		materials.put("ko", "charcoal");
		materials.put("blau", "blue pencil");

		final Map<String, String> writers = new HashMap<String, String>();
		writers.put("g", "Goethe");
		writers.put("ec", "Eckermann");
		writers.put("gt", "Geist");
		writers.put("gh", "Göchhausen");
		writers.put("go", "Göttling");
		writers.put("jo", "John");
		writers.put("kr", "Kräuter");
		writers.put("m", "Müller");
		writers.put("ri", "Riemer");
		writers.put("st", "Schuchardt");
		writers.put("sta", "Stadelmann");
		writers.put("v", "Helene Vulpius");
		writers.put("wejo", "Weller und John");
		writers.put("zs", "Zeitgenössische Schrift");
		writers.put("sc", "Schreiberhand");
		writers.put("xx", "Fremde Hand #1");
		writers.put("xy", "Fremde Hand #2");
		writers.put("xz", "Fremde Hand #3");

		final Map<String, String> writerMaterials = new LinkedHashMap<String, String>();
		writerMaterials.put("g", "t, tr, bl, ro, ko");
		writerMaterials.put("ec", "t, bl");
		writerMaterials.put("gt", "t");
		writerMaterials.put("gh", "t");
		writerMaterials.put("go", "t");
		writerMaterials.put("jo", "t");
		writerMaterials.put("kr", "t");
		writerMaterials.put("m", "t");
		writerMaterials.put("ri", "t, bl");
		writerMaterials.put("st", "t, bl");
		writerMaterials.put("sta", "t");
		writerMaterials.put("v", "t");
		writerMaterials.put("wejo", "t");
		writerMaterials.put("zs", "t");
		writerMaterials.put("sc", "t, tr, bl");
		writerMaterials.put("xx", "t, tr, bl, blau");
		writerMaterials.put("xy", "t, tr, bl, blau");
		writerMaterials.put("xz", "t, tr, bl, blau");

		for (String writer : writerMaterials.keySet())
		{
			String writerName = writers.get(writer);
			for (String material : StringUtils.stripAll(StringUtils.split(writerMaterials.get(writer), ",")))
			{
				String materialDesc = materials.get(material);
				for (String typeface : typefaces.keySet())
				{
					String typefaceDesc = typefaces.get(typeface);
					HAND_DECLARATIONS.put(String.format("%s_%s", writer, material, typeface), String.format("%s (%s)", writerName, materialDesc));
					HAND_DECLARATIONS.put(String.format("%s_%s_%s", writer, material, typeface), String.format("%s (%s - %s)", writerName, materialDesc, typefaceDesc));
				}
			}
		}

		CHAR_DECLARATIONS
				.put(
						"parenthesis_left",
						new CharacterDeclaration(
								"LEFT PARENTHESIS",
								"In 18th century there were different ways to represent parentheses in handwritten documents. "
										+ "Then the token most often used (default) symbol was a vertical stroke combined with a colon. "
										+ "We want to be able to differentiate between this default representation, which we encode as normal parenthesis characters, and the very (same) characters, that are used today, and that also appear in documents of that time. "
										+ "Therefore we encode the  contemporary representation of parantheses, if encountered in handwritten documents, as glyphs.",
								"("));
		CHAR_DECLARATIONS.put("parenthesis_right", new CharacterDeclaration("RIGHT PARENTHESIS", "See the description of LEFT PARANTHESIS.", ")"));
		CHAR_DECLARATIONS.put("truncation", new CharacterDeclaration("TRUNCATION SIGN", "The suspension/truncation sign, in German known as “Abbrechungszeichen”.", "."));

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
