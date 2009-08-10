package de.faustedition.model;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Container {

	protected Container parent;
	protected String name;

	protected Container(Container parent, String name) {
		assert isValidName(name);
		this.parent = parent;
		this.name = name;
	}

	public String getPath() {
		return (parent == null ? "" : parent.getPath()) + "/" + name;
	}

	public String getName() {
		return name;
	}

	public static String normalizeName(String name) {
		name = StringUtils.strip(name, "/").replaceAll(Pattern.quote("/"), "_");

		// umlauts
		name = name.replaceAll("\u00c4", "Ae").replaceAll("\u00e4", "ae");
		name = name.replaceAll("\u00d6", "Oe").replaceAll("\u00f6", "oe");
		name = name.replaceAll("\u00dc", "Ue").replaceAll("\u00fc", "ue");
		name = name.replaceAll("\u00df", "ss");

		// non-printable characters
		name = name.replaceAll("[^\\w\\.\\-]", "_");

		// condense underscores
		name = name.replaceAll("_+", "_");
		return name.trim();
	}

	protected boolean isValidName(String name) {
		return normalizeName(name).equals(name);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
