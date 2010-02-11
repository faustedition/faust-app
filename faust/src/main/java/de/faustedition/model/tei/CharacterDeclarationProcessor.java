package de.faustedition.model.tei;

import static de.faustedition.model.tei.EncodedTextDocument.TEI_NS_URI;
import static de.faustedition.model.tei.EncodedTextDocument.xpath;
import static de.faustedition.model.xmldb.NodeListIterable.singleResult;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.XMLConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.faustedition.util.XMLUtil;

public class CharacterDeclarationProcessor implements EncodedTextDocumentProcessor {
	private static final String LEFT_PARANTHESIS_DESCRIPTION = "In 18th century there were different ways to represent parentheses in handwritten documents. "
			+ "Then the token most often used (default) symbol was a vertical stroke combined with a colon. "
			+ "We want to be able to differentiate between this default representation, which we encode as normal parenthesis characters, and the very (same) characters, that are used today, and that also appear in documents of that time. "
			+ "Therefore we encode the  contemporary representation of parantheses, if encountered in handwritten documents, as glyphs.";
	private static final String RIGHT_PARANTHESIS_DESCRIPTION = "See the description of LEFT PARANTHESIS.";
	private static final String TRUNCATION_SIGN_DESCRIPTION = "The suspension/truncation sign, in German known as “Abbrechungszeichen”.";

	private final Map<String, CharacterDeclaration> declarations = new LinkedHashMap<String, CharacterDeclaration>();

	public CharacterDeclarationProcessor() {
		declarations.put("parenthesis_left",
				new CharacterDeclaration("LEFT PARENTHESIS", LEFT_PARANTHESIS_DESCRIPTION, "("));
		declarations.put("parenthesis_right", new CharacterDeclaration("RIGHT PARENTHESIS", RIGHT_PARANTHESIS_DESCRIPTION,
				")"));
		declarations.put("truncation", new CharacterDeclaration("TRUNCATION SIGN", TRUNCATION_SIGN_DESCRIPTION, "."));
	}

	public void process(EncodedTextDocument teiDocument) {
		Document dom = teiDocument.getDom();
		Element charDeclElement = singleResult(xpath("//tei:charDecl"), dom, Element.class);
		if (charDeclElement == null) {
			Element encodingDescEl = singleResult(xpath("//tei:encodingDesc"), dom, Element.class);
			if (encodingDescEl == null) {
				throw new IllegalStateException();
			}
			if (!XMLUtil.hasText(encodingDescEl)) {
				XMLUtil.removeChildren(encodingDescEl);
			}
			encodingDescEl.appendChild(charDeclElement = dom.createElementNS(TEI_NS_URI, "charDecl"));
		}

		XMLUtil.removeChildren(charDeclElement);

		for (String id : declarations.keySet()) {
			CharacterDeclaration declaration = declarations.get(id);

			Element charElement = dom.createElementNS(TEI_NS_URI, "char");
			charElement.setAttributeNS(XMLConstants.XML_NS_URI, "xml:id", id);
			charDeclElement.appendChild(charElement);

			Element charNameElement = dom.createElementNS(TEI_NS_URI, "charName");
			charElement.appendChild(charNameElement);
			charNameElement.setTextContent(declaration.name);

			Element descElement = dom.createElementNS(TEI_NS_URI, "desc");
			charElement.appendChild(descElement);
			descElement.setTextContent(declaration.description);

			Element mappingElement = dom.createElementNS(TEI_NS_URI, "mapping");
			charElement.appendChild(mappingElement);
			mappingElement.setAttributeNS(TEI_NS_URI, "type", "Unicode");
			mappingElement.setTextContent(declaration.unicodeMapping);
		}

	}

	private static class CharacterDeclaration {
		private String name;
		private String description;
		private String unicodeMapping;

		private CharacterDeclaration(String name, String description, String unicodeMapping) {
			this.name = name;
			this.description = description;
			this.unicodeMapping = unicodeMapping;
		}
	}

}
