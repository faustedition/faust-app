package de.swkk.metadata.inventory;

import static de.faustedition.model.tei.EncodedTextDocument.FAUST_NS_URI;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.faustedition.util.XMLUtil;

public class MetadataRecord extends LinkedHashMap<String, String> {
	private static final Map<String, String> FIELD_MAPPING;
	private static final Set<String> RECORD_FIELD_SET;

	public static MetadataRecord map(Map<String, String> metadata) {
		MetadataRecord record = new MetadataRecord();

		for (String field : metadata.keySet()) {
			String mappedKey = FIELD_MAPPING.get(field);
			if (mappedKey == null) {
				continue;
			}
			record.put(mappedKey, metadata.get(field));
		}

		return record;
	}

	public void toXml(Element parent) {
		Document d = parent.getOwnerDocument();
		for (Map.Entry<String, String> entry : entrySet()) {
			Element field = d.createElementNS(FAUST_NS_URI, entry.getKey());
			field.setTextContent(entry.getValue());
			parent.appendChild(field);
		}
	}

	public static MetadataRecord fromXml(Element parent) {
		FIELD_MAPPING.values();
		MetadataRecord record = new MetadataRecord();
		for (Node node : XMLUtil.iterableNodeList(parent.getChildNodes())) {
			if (!(Node.ELEMENT_NODE == node.getNodeType())) {
				continue;
			}
			Element field = (Element) node;
			if (FAUST_NS_URI.equals(field.getNamespaceURI()) && RECORD_FIELD_SET.contains(field.getLocalName())) {
				record.put(field.getLocalName(), field.getTextContent());
			}
		}
		return record;
	}

	public void merge(MetadataRecord other) {
		for (Map.Entry<String, String> entry : other.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public String put(String key, String value) {
		if (containsKey(key)) {
			String currentValue = get(key);
			value = currentValue + (currentValue.contains(value) ? "" : ("\n" + value));
		}
		return super.put(key, value);
	}

	static {
		FIELD_MAPPING = new HashMap<String, String>();
		FIELD_MAPPING.put("26", "id_bohnenkamp");
		FIELD_MAPPING.put("25", "id_weimarer_ausgabe");
		FIELD_MAPPING.put("41p", "id_paralipomenon_weimarer_ausgabe");
		FIELD_MAPPING.put("41v", "work_verses");
		FIELD_MAPPING.put("41f", "work_missing_verses");
		FIELD_MAPPING.put("333", "work_genetic_level_goethe");
		FIELD_MAPPING.put("06a", "work_genetic_level_custom");
		FIELD_MAPPING.put("911", "location_local");
		FIELD_MAPPING.put("081", "portfolio");
		FIELD_MAPPING.put("912", "callnumber");
		FIELD_MAPPING.put("08", "callnumber_old");
		FIELD_MAPPING.put("089", "callnumber_old");
		FIELD_MAPPING.put("88", "inventory_kraeuter");
		FIELD_MAPPING.put("993", "provenience");
		FIELD_MAPPING.put("71", "first_print");
		FIELD_MAPPING.put("951", "first_print");
		FIELD_MAPPING.put("41z", "print_weimarer_ausgabe");
		FIELD_MAPPING.put("61a", "print_weimarer_ausgabe_additional");
		FIELD_MAPPING.put("61b", "print_weimarer_ausgabe_additional");
		FIELD_MAPPING.put("85", "print_akademie_ausgabe");
		FIELD_MAPPING.put("41b", "print_bohnenkamp");
		FIELD_MAPPING.put("953", "print_frankfurter_ausgabe");
		FIELD_MAPPING.put("92", "print_leopoldina");
		FIELD_MAPPING.put("20", "manuscript_reference_weimarer_ausgabe");
		FIELD_MAPPING.put("86", "manuscript_reference_akademie_ausgabe");
		FIELD_MAPPING.put("93", "manuscript_reference_leopoldina");
		FIELD_MAPPING.put("80", "manuscript_extent");
		FIELD_MAPPING.put("16", "pagenumber_goethe");
		FIELD_MAPPING.put("80a", "material_extent");
		FIELD_MAPPING.put("60", "preservation_state");
		FIELD_MAPPING.put("30", "hand_1");
		FIELD_MAPPING.put("30a", "hand_1_notes");
		FIELD_MAPPING.put("30c", "hand_1_notes");
		FIELD_MAPPING.put("31", "hand_4");
		FIELD_MAPPING.put("30b", "hand_7");
		FIELD_MAPPING.put("07", "contents");
		FIELD_MAPPING.put("42i", "incipit");
		FIELD_MAPPING.put("42j", "incipit");
		FIELD_MAPPING.put("42k", "incipit");
		FIELD_MAPPING.put("42l", "incipit");
		FIELD_MAPPING.put("42m", "incipit");
		FIELD_MAPPING.put("42n", "incipit");
		FIELD_MAPPING.put("42o", "incipit");
		FIELD_MAPPING.put("42p", "incipit");
		FIELD_MAPPING.put("42q", "incipit");
		FIELD_MAPPING.put("223", "incipit");
		FIELD_MAPPING.put("14", "incipit");
		FIELD_MAPPING.put("12a", "title_citation");
		FIELD_MAPPING.put("12i", "dating_normalized");
		FIELD_MAPPING.put("12e", "dating_normalized");
		FIELD_MAPPING.put("12d", "dating_given");
		FIELD_MAPPING.put("06", "remarks");
		FIELD_MAPPING.put("91", "remarks");
		FIELD_MAPPING.put("995", "remarks");
		FIELD_MAPPING.put("90", "remarks");
		FIELD_MAPPING.put("992", "remarks");
		FIELD_MAPPING.put("xx0", "record_number");
		FIELD_MAPPING.put("994", "reproduction_number");

		RECORD_FIELD_SET = new HashSet<String>(FIELD_MAPPING.values());
	}
}
