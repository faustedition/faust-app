package de.swkk.metadata.inventory;

import java.util.HashMap;
import java.util.Map;

import de.faustedition.model.metadata.MetadataRecord;

public class MetadataFieldMapping extends HashMap<String, String> {
	public MetadataFieldMapping() {
		put("26", "id_bohnenkamp");
		put("25", "id_weimarer_ausgabe");
		put("41p", "id_paralipomenon_weimarer_ausgabe");
		put("41v", "work_verses");
		put("41f", "work_missing_verses");
		put("333", "work_genetic_level_goethe");
		put("06a", "work_genetic_level_custom");
		put("911", "location_local");
		put("081", "portfolio");
		put("912", "callnumber");
		put("08", "callnumber_old");
		put("089", "callnumber_old");
		put("88", "inventory_kraeuter");
		put("993", "provenience");
		put("71", "first_print");
		put("951", "first_print");
		put("41z", "print_weimarer_ausgabe");
		put("61a", "print_weimarer_ausgabe_additional");
		put("61b", "print_weimarer_ausgabe_additional");
		put("85", "print_akademie_ausgabe");
		put("41b", "print_bohnenkamp");
		put("953", "print_frankfurter_ausgabe");
		put("92", "print_leopoldina");
		put("20", "manuscript_reference_weimarer_ausgabe");
		put("86", "manuscript_reference_akademie_ausgabe");
		put("93", "manuscript_reference_leopoldina");
		put("80", "manuscript_extent");
		put("16", "pagenumber_goethe");
		put("80a", "material_extent");
		put("60", "preservation_state");
		put("30", "hand_1");
		put("30a", "hand_1_notes");
		put("30c", "hand_1_notes");
		put("31", "hand_4");
		put("30b", "hand_7");
		put("07", "contents");
		put("42i", "incipit");
		put("42j", "incipit");
		put("42k", "incipit");
		put("42l", "incipit");
		put("42m", "incipit");
		put("42n", "incipit");
		put("42o", "incipit");
		put("42p", "incipit");
		put("42q", "incipit");
		put("223", "incipit");
		put("14", "incipit");
		put("12a", "title_citation");
		put("12i", "dating_normalized");
		put("12e", "dating_normalized");
		put("12d", "dating_given");
		put("06", "remarks");
		put("91", "remarks");
		put("995", "remarks");
		put("90", "remarks");
		put("992", "remarks");
		put("xx0", "record_number");
		put("994", "reproduction_number");
	}

	public MetadataRecord map(Map<String, String> metadata) {
		MetadataRecord record = new MetadataRecord();

		for (String field : metadata.keySet()) {
			String mappedKey = get(field);
			if (mappedKey == null) {
				continue;
			}
			record.put(mappedKey, metadata.get(field));
		}

		return record;
	}
}
