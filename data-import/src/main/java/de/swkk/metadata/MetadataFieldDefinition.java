package de.swkk.metadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class MetadataFieldDefinition implements Comparable<MetadataFieldDefinition> {
	public static Map<MetadataFieldGroup, List<MetadataFieldDefinition>> REGISTRY;
	public static Map<String, MetadataFieldDefinition> REGISTRY_LOOKUP_TABLE;

	private MetadataFieldGroup group;
	private int order;
	private String name;
	private Class<?> type;
	private MetadataAnnotationLevel lowestLevel;

	private MetadataFieldDefinition(MetadataFieldGroup group, int order, String name, Class<?> type,
			MetadataAnnotationLevel lowestLevel) {
		this.group = group;
		this.order = order;
		this.name = name;
		this.type = type;
		this.lowestLevel = lowestLevel;
	}

	public MetadataFieldGroup getGroup() {
		return group;
	}

	public int getOrder() {
		return order;
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}

	public MetadataAnnotationLevel getLowestLevel() {
		return lowestLevel;
	}

	public int compareTo(MetadataFieldDefinition o) {
		return (group.equals(o.group) ? (order - o.order) : (group.ordinal() - o.group.ordinal()));
	}

	public static SortedMap<MetadataFieldGroup, MetadataRecord> createStructuredMetadata(MetadataRecord metadata) {
		SortedMap<MetadataFieldGroup, MetadataRecord> metadataStructure = new TreeMap<MetadataFieldGroup, MetadataRecord>();
		for (String fieldKey : metadata.keySet()) {
			MetadataFieldGroup fieldGroup = REGISTRY_LOOKUP_TABLE.get(fieldKey).getGroup();
			if (metadataStructure.containsKey(fieldGroup)) {
				metadataStructure.get(fieldGroup).put(fieldKey, metadata.get(fieldKey));
			} else {
				MetadataRecord groupRecord = new MetadataRecord();
				groupRecord.put(fieldKey, metadata.get(fieldKey));
				metadataStructure.put(fieldGroup, groupRecord);
			}
		}
		return metadataStructure;
	}

	static {
		REGISTRY = new HashMap<MetadataFieldGroup, List<MetadataFieldDefinition>>();
		REGISTRY_LOOKUP_TABLE = new HashMap<String, MetadataFieldDefinition>();

		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, 1, "location_local", String.class,
				MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, 2, "location_organization", String.class,
				MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, 3, "portfolio", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, 4, "callnumber", String.class,
				MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, 5, "callnumber_old", String.class,
				MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, 6, "inventory_kraeuter", String.class,
				MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, 7, "provenience", String.class,
				MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, 8, "inventory_weimarer_ausgabe", String.class,
				MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, 9, "bibliographic_notes", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, 10, "first_print", String.class,
				MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, 11, "first_print_comments", String.class,
				MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, 12, "autotype", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, 13, "bundle_extent", String.class,
				MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, 14, "bundle_contents", String.class,
				MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.REFERENCE, 1, "id_bohnenkamp", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.REFERENCE, 2, "id_weimarer_ausgabe", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.REFERENCE, 3, "id_additional", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.REFERENCE, 4, "id_paralipomenon_weimarer_ausgabe",
				String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.REFERENCE, 5, "reference_generic", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.REFERENCE, 6, "work_act_scene", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.REFERENCE, 7, "work_verses", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.REFERENCE, 8, "work_missing_verses", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.REFERENCE, 9, "work_genetic_level_goethe", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.REFERENCE, 10, "work_genetic_level_custom", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, 1, "dimension", String.class,
				MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, 2, "material_extent", String.class,
				MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, 3, "format", String.class,
				MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, 4, "format_original", String.class,
				MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, 5, "edges", String.class,
				MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, 6, "cropping", String.class,
				MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, 7, "folding", String.class,
				MetadataAnnotationLevel.SHEET));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, 8, "paper_attributes", String.class,
				MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, 9, "sticky_notes", String.class,
				MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, 10, "paper_type", String.class,
				MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, 11, "color", String.class,
				MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, 12, "watermark", String.class,
				MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, 13, "preservation_notes", String.class,
				MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, 14, "material_features", String.class,
				MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, 15, "preservation_state", String.class,
				MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, 1, "hand_1", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, 2, "hand_1_notes", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, 3, "hand_2", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, 4, "hand_2_notes", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, 5, "hand_3", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, 6, "hand_3_notes", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, 7, "hand_4", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, 8, "hand_4_notes", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, 9, "hand_5", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, 10, "hand_5_notes", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, 11, "hand_6", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, 12, "hand_6_notes", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, 13, "hand_7", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, 14, "hand_7_notes", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, 15, "hand_7_notes", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, 16, "script", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, 17, "writing_materials", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, 18, "other_hand", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, 19, "other_script", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, 20, "other_writing_materials", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.TEXT, 1, "contents", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.TEXT, 2, "contents_paratextual", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.TEXT, 3, "incipit", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.TEXT, 4, "title_citation", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.TEXT, 5, "genre", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.TEXT, 6, "dating_normalized", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.TEXT, 7, "dating_given", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MANUSCRIPT, 1, "manuscript_reference_weimarer_ausgabe",
				String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MANUSCRIPT, 2, "manuscript_reference_bohnenkamp",
				String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MANUSCRIPT, 3, "manuscript_reference_landeck",
				String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MANUSCRIPT, 4, "manuscript_reference_fischer_lamberg",
				String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MANUSCRIPT, 5, "manuscript_reference_akademie_ausgabe",
				String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MANUSCRIPT, 6, "manuscript_reference_leopoldina",
				String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MANUSCRIPT, 7, "manuscript_extent", String.class,
				MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MANUSCRIPT, 8, "pagenumber_goethe", String.class,
				MetadataAnnotationLevel.PAGE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MANUSCRIPT, 9, "pagenumber", String.class,
				MetadataAnnotationLevel.PAGE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MANUSCRIPT, 10, "pagenumber_canonical", String.class,
				MetadataAnnotationLevel.PAGE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.PRINTS, 1, "print_weimarer_ausgabe", String.class,
				MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.PRINTS, 2, "print_weimarer_ausgabe_additional",
				String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.PRINTS, 3, "print_akademie_ausgabe", String.class,
				MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.PRINTS, 4, "print_bohnenkamp", String.class,
				MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.PRINTS, 5, "print_landeck", String.class,
				MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.PRINTS, 6, "print_frankfurter_ausgabe", String.class,
				MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.PRINTS, 7, "print_leopoldina", String.class,
				MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.SUPPLEMENT, 1, "remarks", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.SUPPLEMENT, 2, "record_number", String.class,
				MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.SUPPLEMENT, 3, "reproduction_number", String.class,
				MetadataAnnotationLevel.ITEM));

		REGISTRY = Collections.unmodifiableMap(REGISTRY);
		REGISTRY_LOOKUP_TABLE = Collections.unmodifiableMap(REGISTRY_LOOKUP_TABLE);
	}

	private static void register(MetadataFieldDefinition definition) {
		if (!REGISTRY.containsKey(definition.getGroup())) {
			REGISTRY.put(definition.getGroup(), new LinkedList<MetadataFieldDefinition>());
		}
		REGISTRY.get(definition.getGroup()).add(definition);
		REGISTRY_LOOKUP_TABLE.put(definition.getName(), definition);
	}
}
