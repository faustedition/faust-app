package de.faustedition.web.metadata;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MetadataFieldDefinition {
	private static Map<MetadataFieldGroup, List<MetadataFieldDefinition>> REGISTRY;
	private static Map<String, MetadataFieldDefinition> REGISTRY_LOOKUP_TABLE;

	private MetadataFieldGroup group;
	private String name;
	private Class<?> type;
	private MetadataAnnotationLevel lowestLevel;

	private MetadataFieldDefinition(MetadataFieldGroup group, String name, Class<?> type, MetadataAnnotationLevel lowestLevel) {
		this.group = group;
		this.name = name;
		this.type = type;
		this.lowestLevel = lowestLevel;
	}

	public MetadataFieldGroup getGroup() {
		return group;
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

	private static void register(MetadataFieldDefinition definition) {
		if (!REGISTRY.containsKey(definition.getGroup())) {
			REGISTRY.put(definition.getGroup(), new LinkedList<MetadataFieldDefinition>());
		}
		REGISTRY.get(definition.getGroup()).add(definition);
		REGISTRY_LOOKUP_TABLE.put(definition.getName(), definition);
	}

	static {
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, "location_local", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, "location_organization", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, "portfolio", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, "callnumber", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, "callnumber_old", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, "inventory_kraeuter", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, "provenience", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, "inventory_weimarer_ausgabe", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, "bibliographic_notes", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, "first_print", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, "first_print_comments", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, "autotype", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, "bundle_extent", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.ORIGIN, "bundle_contents", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.REFERENCE, "id_bohnenkamp", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.REFERENCE, "id_weimarer_ausgabe", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.REFERENCE, "id_additional", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.REFERENCE, "id_paralipomenon_weimarer_ausgabe", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.REFERENCE, "reference_generic", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.REFERENCE, "work_act_scene", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.REFERENCE, "work_verses", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.REFERENCE, "work_missing_verses", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.REFERENCE, "work_genetic_level_goethe", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.REFERENCE, "work_genetic_level_custom", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, "dimension", String.class, MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, "material_extent", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, "format", String.class, MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, "format_original", String.class, MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, "edges", String.class, MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, "cropping", String.class, MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, "folding", String.class, MetadataAnnotationLevel.SHEET));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, "paper_attributes", String.class, MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, "sticky_notes", String.class, MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, "paper_type", String.class, MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, "color", String.class, MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, "watermark", String.class, MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, "preservation_notes", String.class, MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, "material_features", String.class, MetadataAnnotationLevel.LEAF));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MATERIAL, "preservation_state", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, "hand_1", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, "hand_1_notes", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, "hand_2", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, "hand_2_notes", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, "hand_3", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, "hand_3_notes", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, "hand_4", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, "hand_4_notes", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, "hand_5", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, "hand_5_notes", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, "hand_6", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, "hand_6_notes", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, "hand_7", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, "hand_7_notes", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, "hand_7_notes", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, "script", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, "writing_materials", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, "other_hand", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, "other_script", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.INSCRIPTION, "other_writing_materials", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.TEXT, "contents", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.TEXT, "contents_paratextual", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.TEXT, "incipit", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.TEXT, "title_citation", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.TEXT, "genre", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.TEXT, "dating_normalized", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.TEXT, "dating_given", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MANUSCRIPT, "manuscript_reference_weimarer_ausgabe", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MANUSCRIPT, "manuscript_reference_bohnenkamp", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MANUSCRIPT, "manuscript_reference_landeck", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MANUSCRIPT, "manuscript_reference_fischer_lamberg", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MANUSCRIPT, "manuscript_reference_akademie_ausgabe", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MANUSCRIPT, "manuscript_reference_leopoldina", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MANUSCRIPT, "manuscript_extent", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MANUSCRIPT, "pagenumber_goethe", String.class, MetadataAnnotationLevel.PAGE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MANUSCRIPT, "pagenumber", String.class, MetadataAnnotationLevel.PAGE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.MANUSCRIPT, "pagenumber_canonical", String.class, MetadataAnnotationLevel.PAGE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.PRINTS, "print_weimarer_ausgabe", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.PRINTS, "print_weimarer_ausgabe_additional", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.PRINTS, "print_akademie_ausgabe", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.PRINTS, "print_bohnenkamp", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.PRINTS, "print_landeck", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.PRINTS, "print_frankfurter_ausgabe", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.PRINTS, "print_leopoldina", String.class, MetadataAnnotationLevel.FILE));
		register(new MetadataFieldDefinition(MetadataFieldGroup.SUPPLEMENT, "remarks", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.SUPPLEMENT, "record_number", String.class, MetadataAnnotationLevel.ITEM));
		register(new MetadataFieldDefinition(MetadataFieldGroup.SUPPLEMENT, "reproduction_number", String.class, MetadataAnnotationLevel.ITEM));
	}
}
