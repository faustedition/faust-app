package de.faustedition.document;

public enum WritingMaterial {
	INK("t", "ink"), //
	INK_RED("tr", "ink red/brown"), //
	PENCIL("bl", "pencil"), //
	RUDDLE("ro", "ruddle"), //
	CHARCOAL("ko", "charcoal"), //
	BLUE("blau", "blue pencil");

	private final String key;
	private final String description;

	private WritingMaterial(String key, String description) {
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
