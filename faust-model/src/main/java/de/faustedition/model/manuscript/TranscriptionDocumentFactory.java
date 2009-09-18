package de.faustedition.model.manuscript;

import static de.faustedition.model.TEIDocument.teiElementNode;
import static net.sf.practicalxml.builder.XmlBuilder.attribute;
import static net.sf.practicalxml.builder.XmlBuilder.text;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.ParseUtil;
import net.sf.practicalxml.XmlUtil;
import net.sf.practicalxml.builder.ElementNode;
import net.sf.practicalxml.builder.Node;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.faustedition.model.TEIDocument;

public class TranscriptionDocumentFactory {
	private static final String SCHEMA_URI_DEFAULT = "http://www.faustedition.net/schema/v1/faust-tei.rnc";
	private static final String STYLESHEET_URI_DEFAULT = "http://www.faustedition.net/schema/v1/faust-tei.css";
	private static final Map<String, String> HAND_DECLARATIONS = new LinkedHashMap<String, String>();
	private static final Map<String, CharacterDeclaration> CHAR_DECLARATIONS = new LinkedHashMap<String, CharacterDeclaration>();
	private String schemaUri;
	private String stylesheetUri;

	public TranscriptionDocumentFactory(String schemaUri, String stylesheetUri) {
		this.schemaUri = schemaUri;
		this.stylesheetUri = stylesheetUri;
	}

	public TranscriptionDocumentFactory() {
		this.schemaUri = SCHEMA_URI_DEFAULT;
		this.stylesheetUri = STYLESHEET_URI_DEFAULT;
	}

	public TranscriptionDocument parse(InputStream stream) {
		return new TranscriptionDocument(ParseUtil.parse(new InputSource(stream)));
	}

	public TranscriptionDocument build(Transcription transcription) {
		Document document = buildTemplate(transcription);

		Document transcriptionDataDocument = ParseUtil.parse(new InputSource(new ByteArrayInputStream(transcription.getTextData())));
		document.getDocumentElement().appendChild(document.importNode(transcriptionDataDocument.getDocumentElement(), true));

		Document revisionDataDocument = ParseUtil.parse(new InputSource(new ByteArrayInputStream(transcription.getRevisionData())));
		DomUtil.getChild(document.getDocumentElement(), "teiHeader").appendChild(document.importNode(revisionDataDocument.getDocumentElement(), true));
		
		return new TranscriptionDocument(document);
	}

	protected Document buildTemplate(Transcription transcription) {
		Manuscript manuscript = transcription.getFacsimile().getManuscript();
		ElementNode titleNode = teiElementNode("title", text(manuscript.getPortfolio().getName() + "-" + manuscript.getName()));
		ElementNode fileDesc = teiElementNode("fileDesc", teiElementNode("titleStmt", titleNode), teiElementNode("sourceDesc", teiElementNode("p")));
		ElementNode encodingDesc = teiElementNode("encodingDesc", characterDeclarations());
		ElementNode profileDesc = teiElementNode("profileDesc", handDeclarations());

		Document document = DomUtil.newDocument();
		document.appendChild(document.createProcessingInstruction("xml-stylesheet", String.format("href=\"%s\" type=\"text/css\"", XmlUtil.escape(stylesheetUri))));
		document.appendChild(document.createProcessingInstruction("oxygen", String.format("RNGSchema=\"%s\" type=\"compact\"", XmlUtil.escape(schemaUri))));
		document.appendChild(document.importNode(TEIDocument.teiRoot(teiElementNode("teiHeader", fileDesc, encodingDesc, profileDesc)).toDOM().getDocumentElement(), true));
		return document;
	}

	private Node characterDeclarations() {
		List<Node> declarations = new ArrayList<Node>(CHAR_DECLARATIONS.size());
		for (String id : CHAR_DECLARATIONS.keySet()) {
			CharacterDeclaration declaration = CHAR_DECLARATIONS.get(id);

			ElementNode charNameNode = teiElementNode("charName", text(declaration.name));
			ElementNode descNode = teiElementNode("desc", text(declaration.description));
			ElementNode mappingNode = teiElementNode("mapping", attribute("type", "Unicode"), text(declaration.unicodeMapping));

			declarations.add(teiElementNode("char", attribute(XMLConstants.XML_NS_URI, "xml:id", id), charNameNode, descNode, mappingNode));
		}

		return teiElementNode("charDecl", declarations.toArray(new Node[declarations.size()]));
	}

	private Node handDeclarations() {
		List<Node> declarations = new ArrayList<Node>(HAND_DECLARATIONS.size());
		for (String id : HAND_DECLARATIONS.keySet()) {
			declarations.add(teiElementNode("handNote", attribute(XMLConstants.XML_NS_URI, "xml:id", id), text(HAND_DECLARATIONS.get(id))));
		}
		return teiElementNode("handNotes", declarations.toArray(new Node[declarations.size()]));
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

		for (String writer : writerMaterials.keySet()) {
			String writerName = writers.get(writer);
			for (String material : StringUtils.stripAll(StringUtils.split(writerMaterials.get(writer), ","))) {
				String materialDesc = materials.get(material);
				for (String typeface : typefaces.keySet()) {
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
