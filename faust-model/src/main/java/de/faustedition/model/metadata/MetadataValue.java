package de.faustedition.model.metadata;

public class MetadataValue {
	private String field;
	private String value;

	public MetadataValue(String field, String value) {
		assert MetadataFieldDefinition.REGISTRY.containsKey(field);
		this.field = field;
		this.value = value;
	}

	public String getField() {
		return field;
	}

	public String getValue() {
		return value;
	}

	public MetadataFieldDefinition getDefinition() {
		return MetadataFieldDefinition.REGISTRY_LOOKUP_TABLE.get(field);
	}

	public MetadataValue aggregate(MetadataValue metadataValue) {
		assert field.equals(metadataValue.field);
		return new MetadataValue(field, value + "\n" + metadataValue.value);
	}
}
