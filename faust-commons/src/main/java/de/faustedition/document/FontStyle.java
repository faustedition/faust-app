package de.faustedition.document;

public enum FontStyle {
	SCHRIFT(null, "Schrift"), //
	LATIN("lat", "latin"), //
	GREEK("gr", "greek");

	private final String key;
	private final String description;

	private FontStyle(String key, String description) {
		this.key = key;
		this.description = description;
	}

	public String getKey() {
		return key;
	}

	public String getDescription() {
		return description;
	}
}
