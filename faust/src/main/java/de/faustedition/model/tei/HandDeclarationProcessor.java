package de.faustedition.model.tei;

import static de.faustedition.model.tei.EncodedTextDocument.TEI_NS_URI;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.XMLConstants;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.faustedition.util.XMLUtil;

public class HandDeclarationProcessor implements EncodedTextDocumentProcessor {
	private final Map<String, String> declarations = new LinkedHashMap<String, String>();

	public HandDeclarationProcessor() {
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
					declarations.put(String.format("%s_%s", writer, material, typeface), String.format(
							"%s (%s)", writerName, materialDesc));
					declarations.put(String.format("%s_%s_%s", writer, material, typeface), String.format(
							"%s (%s - %s)", writerName, materialDesc, typefaceDesc));
				}
			}
		}
	}

	@Override
	public void process(EncodedTextDocument teiDocument) {
		Document domDocument = teiDocument.getDocument();

		Element handNotesElement = teiDocument.findElementByPath("teiHeader", "profileDesc", "handNotes");
		if (handNotesElement == null) {
			Element profileDescElement = teiDocument.findElementByPath("teiHeader", "profileDesc");
			if (profileDescElement == null) {
				throw new IllegalStateException();
			}
			for (Node childNode : XMLUtil.iterableNodeList(profileDescElement.getChildNodes())) {
				if ((Node.ELEMENT_NODE == childNode.getNodeType()) && "p".equals(childNode.getLocalName())) {
					if (!XMLUtil.hasText((Element) childNode)) {
						profileDescElement.removeChild(childNode);
					}
				}
			}
			profileDescElement.appendChild(handNotesElement = domDocument.createElementNS(TEI_NS_URI, "handNotes"));
		}

		XMLUtil.removeChildren(handNotesElement);

		for (String id : declarations.keySet()) {
			Element handNoteElement = domDocument.createElementNS(TEI_NS_URI, "handNote");
			handNotesElement.appendChild(handNoteElement);

			handNoteElement.setTextContent(declarations.get(id));
			handNoteElement.setAttributeNS(XMLConstants.XML_NS_URI, "xml:id", id);
		}

	}

}
