package de.faustedition.model.metadata;

import java.util.Map;

import de.faustedition.model.store.AbstractContentObject;
import de.faustedition.model.store.ContentObject;

public class MetadataBundle extends AbstractContentObject {
	public static final String METADATA_CONTENT_NAME = "metadata";
	
	private Map<String, String> values;

	public MetadataBundle(String path, String name) {
		super(path, name);
		assert METADATA_CONTENT_NAME.equals(name);
	}

	public MetadataBundle(ContentObject parent) {
		super(parent, METADATA_CONTENT_NAME);
	}

	public Map<String, String> getValues() {
		return values;
	}

	public void setValues(Map<String, String> values) {
		this.values = values;
	}
}
