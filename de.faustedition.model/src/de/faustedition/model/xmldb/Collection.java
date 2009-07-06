package de.faustedition.model.xmldb;

import org.apache.commons.lang.StringUtils;

public class Collection {
	public static final Collection ROOT = new Collection("");
	
	private String path;

	public Collection(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public String createEntryPath(String name) {
		return StringUtils.join(new String[] { path, normalizeName(name) }, "/");
	}

	public Collection createEntry(String name) {
		return new Collection(createEntryPath(name));
	}

	public static String normalizeName(String name) {
		if (name == null) {
			return null;
		}

		// remove leading/trailing separators; replace intermediate ones
		name = StringUtils.strip(name, "/");
		name = name.replaceAll("/", "_");

		// Umlaute
		name = name.replaceAll("\u00c4", "Ae");
		name = name.replaceAll("\u00e4", "ae");
		name = name.replaceAll("\u00d6", "Oe");
		name = name.replaceAll("\u00f6", "oe");
		name = name.replaceAll("\u00dc", "Ue");
		name = name.replaceAll("\u00fc", "ue");
		name = name.replaceAll("\u00df", "ss");

		// non-printable characters
		name = name.replaceAll("[^\\p{Alnum}\\.\\-]", "_");

		// condense underscores
		name = name.replaceAll("_+", "_");

		return StringUtils.trimToEmpty(name);
	}

}
