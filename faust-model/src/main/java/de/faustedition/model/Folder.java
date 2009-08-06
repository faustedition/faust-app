package de.faustedition.model;

import java.io.File;

import org.apache.commons.lang.StringUtils;

public class Folder {
	private File file;

	public Folder(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
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
		name = name.replaceAll("[^\\p{Alnum}]", "_");

		// condense underscores
		name = name.replaceAll("_+", "_");
		
		return StringUtils.trimToNull(name);
	}
}
