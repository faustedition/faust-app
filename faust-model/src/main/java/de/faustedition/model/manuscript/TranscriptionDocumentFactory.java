package de.faustedition.model.manuscript;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.XMLConstants;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ProcessingInstruction;

import org.apache.commons.lang.StringUtils;

public class TranscriptionDocumentFactory {
	private static final String TEI_NS_URI = "http://www.tei-c.org/ns/1.0";
	private static final String SVG_NS_URI = "http://www.w3.org/2000/svg";
	private static final String RELAX_NG_SCHEMA_URI = "http://www.faustedition.net/schema/v1/faust-tei.rnc";
	private static final String CSS_STYLESHEET_URI = "http://www.faustedition.net/schema/v1/faust-tei.css";

	private static final Map<String, String> HAND_DECLARATIONS = new LinkedHashMap<String, String>();
	private static final Map<String, CharacterDeclaration> CHAR_DECLARATIONS = new LinkedHashMap<String, CharacterDeclaration>();

	public TranscriptionDocument build(Transcription transcription) {
		Element teiRoot = new Element("TEI", TEI_NS_URI);
		teiRoot.addNamespaceDeclaration("svg", SVG_NS_URI);
		teiRoot.appendChild(buildHeader(transcription));
		teiRoot.appendChild(buildText(transcription));

		Document document = new Document(teiRoot);
		document.insertChild(new ProcessingInstruction("oxygen", String.format("RNGSchema=\"%s\" type=\"compact\"", RELAX_NG_SCHEMA_URI)), 0);
		document.insertChild(new ProcessingInstruction("xml-stylesheet", String.format("href=\"%s\" type=\"text/css\"", CSS_STYLESHEET_URI)), 0);
		return new TranscriptionDocument(document);
	}

	protected Element buildHeader(Transcription transcription) {
		Element title = new Element("title", TEI_NS_URI);
		Manuscript manuscript = transcription.getFacsimile().getManuscript();
		title.appendChild(manuscript.getPortfolio().getName() + "-" + manuscript.getName());

		Element titleStmt = new Element("titleStmt", TEI_NS_URI);
		titleStmt.appendChild(title);

		Element fileDesc = new Element("fileDesc", TEI_NS_URI);
		fileDesc.appendChild(titleStmt);

		Element encodingDesc = new Element("encodingDesc", TEI_NS_URI);
		addCharacterDeclarations(encodingDesc);

		Element profileDesc = new Element("profileDesc", TEI_NS_URI);
		addHandDeclarations(profileDesc);

		Element sourceDesc = new Element("sourceDesc", TEI_NS_URI);
		sourceDesc.appendChild(new Element("p", TEI_NS_URI));

		Element teiHeader = new Element("teiHeader", TEI_NS_URI);
		teiHeader.appendChild(fileDesc);
		teiHeader.appendChild(encodingDesc);
		teiHeader.appendChild(profileDesc);
		teiHeader.appendChild(sourceDesc);
		return teiHeader;
	}

	protected Element buildText(Transcription transcription) {
		Element body = new Element("body", TEI_NS_URI);
		body.appendChild(new Element("p", TEI_NS_URI));

		Element text = new Element("text", TEI_NS_URI);
		text.appendChild(body);
		return text;
	}

	protected void addHandDeclarations(Element profileDesc) {
		Element handNotes = new Element("handNotes", TEI_NS_URI);
		for (String id : HAND_DECLARATIONS.keySet()) {
			Element handNote = new Element("handNote", TEI_NS_URI);
			handNote.addAttribute(new Attribute("xml:id", XMLConstants.XML_NS_URI, id));
			handNote.appendChild(HAND_DECLARATIONS.get(id));

			handNotes.appendChild(handNote);
		}
		profileDesc.appendChild(handNotes);
	}

	protected void addCharacterDeclarations(Element encodingDesc) {
		Element charDecls = new Element("charDecls", TEI_NS_URI);
		for (String id : CHAR_DECLARATIONS.keySet()) {
			CharacterDeclaration declaration = CHAR_DECLARATIONS.get(id);

			Element charName = new Element("charName", TEI_NS_URI);
			charName.appendChild(declaration.name);

			Element charDesc = new Element("desc", TEI_NS_URI);
			charDesc.appendChild(declaration.description);

			Element charMapping = new Element("mapping", TEI_NS_URI);
			charMapping.addAttribute(new Attribute("type", "Unicode"));
			charMapping.appendChild(declaration.unicodeMapping);

			Element charDecl = new Element("charDecl", TEI_NS_URI);
			charDecl.addAttribute(new Attribute("xml:id", XMLConstants.XML_NS_URI, id));
			charDecl.appendChild(charName);
			charDecl.appendChild(charDesc);
			charDecl.appendChild(charMapping);

			charDecls.appendChild(charDecl);
		}

		encodingDesc.appendChild(charDecls);
	}

	static {
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
		writers.put("wejo", "Weller und John");
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
		writerMaterials.put("wejo", "t");
		writerMaterials.put("sc", "t, tr, bl");
		writerMaterials.put("xx", "t, tr, bl, blau");
		writerMaterials.put("xy", "t, tr, bl, blau");
		writerMaterials.put("xz", "t, tr, bl, blau");

		for (String writer : writerMaterials.keySet()) {
			String writerName = writers.get(writer);
			for (String material : StringUtils.stripAll(StringUtils.split(writerMaterials.get(writer), ","))) {
				String materialDesc = materials.get(material);
				for (String typeface : typefaces.keySet()) {
					String typefaceDesc = typefaces.get(typeface);
					HAND_DECLARATIONS.put(String.format("%s_%s_%s", writer, material, typeface), String.format("%s (%s - %s)", writerName, materialDesc, typefaceDesc));
				}
			}
		}

		CHAR_DECLARATIONS
				.put(
						"parenthesis_left",
						new CharacterDeclaration(
								"LEFT PARENTHESIS",
								"In 18th century there were different ways to represent parentheses in handwritten documents."
										+ "Then the token most often used (default) symbol was a vertical stroke combined with a colon. "
										+ "We want to be able to differentiate between this default representation, which we encode as normal parenthesis characters, and the very (same) characters, that are used today, and that also appear in documents of that time. "
										+ "Therefore we encode the  contemporary representation of parantheses, if encountered in handwritten documents, as glyphs.",
								"("));
		CHAR_DECLARATIONS.put("parenthesis_right", new CharacterDeclaration("RIGHT PARENTHESIS", "See the description of LEFT PARANTHESIS.", ")"));
		CHAR_DECLARATIONS.put("truncation", new CharacterDeclaration("TRUNCATION SIGN", "The suspension/truncation sign, in German known as “Abbrechungszeichen”.", "."));

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
