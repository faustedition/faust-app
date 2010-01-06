package de.faustedition.model.tei;

import static de.faustedition.model.tei.EncodedDocument.TEI_NS_URI;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.XMLConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.faustedition.util.XMLUtil;

public class CharacterDeclarationProcessor implements EncodedDocumentProcessor
{
	private static final String LEFT_PARANTHESIS_DESCRIPTION = "In 18th century there were different ways to represent parentheses in handwritten documents. "
			+ "Then the token most often used (default) symbol was a vertical stroke combined with a colon. "
			+ "We want to be able to differentiate between this default representation, which we encode as normal parenthesis characters, and the very (same) characters, that are used today, and that also appear in documents of that time. "
			+ "Therefore we encode the  contemporary representation of parantheses, if encountered in handwritten documents, as glyphs.";
	private static final String RIGHT_PARANTHESIS_DESCRIPTION = "See the description of LEFT PARANTHESIS.";
	private static final String TRUNCATION_SIGN_DESCRIPTION = "The suspension/truncation sign, in German known as “Abbrechungszeichen”.";

	private final Map<String, CharacterDeclaration> declarations = new LinkedHashMap<String, CharacterDeclaration>();

	public CharacterDeclarationProcessor()
	{
		declarations.put("parenthesis_left", new CharacterDeclaration("LEFT PARENTHESIS", LEFT_PARANTHESIS_DESCRIPTION, "("));
		declarations.put("parenthesis_right", new CharacterDeclaration("RIGHT PARENTHESIS", RIGHT_PARANTHESIS_DESCRIPTION, ")"));
		declarations.put("truncation", new CharacterDeclaration("TRUNCATION SIGN", TRUNCATION_SIGN_DESCRIPTION, "."));
	}

	public void process(EncodedDocument teiDocument)
	{
		Document domDocument = teiDocument.getDocument();
		Element charDeclElement = teiDocument.findElementByPath("teiHeader", "encodingDesc", "charDecl");
		if (charDeclElement == null)
		{
			Element encodingDescElement = teiDocument.findElementByPath("teiHeader", "encodingDesc");
			if (encodingDescElement == null)
			{
				throw new IllegalStateException();
			}
			if (!XMLUtil.hasText(encodingDescElement))
			{
				XMLUtil.removeChildren(encodingDescElement);
			}
			encodingDescElement.appendChild(charDeclElement = domDocument.createElementNS(TEI_NS_URI, "charDecl"));
		}

		XMLUtil.removeChildren(charDeclElement);

		for (String id : declarations.keySet())
		{
			CharacterDeclaration declaration = declarations.get(id);

			Element charElement = domDocument.createElementNS(TEI_NS_URI, "char");
			charElement.setAttributeNS(XMLConstants.XML_NS_URI, "xml:id", id);
			charDeclElement.appendChild(charElement);

			Element charNameElement = domDocument.createElementNS(TEI_NS_URI, "charName");
			charElement.appendChild(charNameElement);
			charNameElement.setTextContent(declaration.name);

			Element descElement = domDocument.createElementNS(TEI_NS_URI, "desc");
			charElement.appendChild(descElement);
			descElement.setTextContent(declaration.description);

			Element mappingElement = domDocument.createElementNS(TEI_NS_URI, "mapping");
			charElement.appendChild(mappingElement);
			mappingElement.setAttributeNS(TEI_NS_URI, "type", "Unicode");
			mappingElement.setTextContent(declaration.unicodeMapping);
		}

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

}
