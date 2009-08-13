package de.faustedition.model.store;

import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

public class ContentStoreUtil {

	public static String toString(Node node) throws RepositoryException {
		ToStringBuilder toStringBuilder = new ToStringBuilder(node);
		toStringBuilder.append("path", node.getPath());
		toStringBuilder.append("type", node.getPrimaryNodeType().getName());
		
		NodeIterator childNodeIt = node.getNodes();
		String[] childNodeNames = new String[(int) childNodeIt.getSize()];
		while (childNodeIt.hasNext()) {
			childNodeNames[(int) childNodeIt.getPosition()] = childNodeIt.nextNode().getName();
		}
		toStringBuilder.append("childNodes", "{" + StringUtils.join(childNodeNames, "; ") + "}");
		
		for (PropertyIterator propertyIterator = node.getProperties(); propertyIterator.hasNext();) {
			Property property = propertyIterator.nextProperty();
			String propertyName = property.getName();
			try {
				if (property.getType() == PropertyType.BINARY) {
					toStringBuilder.append(propertyName, "<binary>");
				} else {
					toStringBuilder.append(propertyName, property.getValue().getString());
				}
			} catch (ValueFormatException e) {
				Value[] values = property.getValues();
				String[] stringValues = new String[values.length];
				for (int i = 0; i < values.length; i++) {
					stringValues[i] = values[i].getString();
				}
				toStringBuilder.append(propertyName, "{ " + StringUtils.join(stringValues, "; ") + " }");
			}
		}
		return toStringBuilder.toString();
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

	public static boolean isValidName(String name) {
		return normalizeName(name).equals(name);
	}

	public static int compare(AbstractContentObject o1, ContentObject o2) {
		return o1.getPath().compareTo(o2.getPath());
	}

	public static String getPath(ContentObject parent, String name) {
		return (parent == null ? "" : parent.getPath() + "/") + name;
	}

	public static String normalizePath(String path) {
		return StringUtils.trimToNull(StringUtils.strip(StringUtils.defaultString(path), "/").replaceAll("/+", "/"));
	}

	public static String[] splitPath(String path) {
		return StringUtils.splitByWholeSeparator(normalizePath(path), "/");
	}
}
