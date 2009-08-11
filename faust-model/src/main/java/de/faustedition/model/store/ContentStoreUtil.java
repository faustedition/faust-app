package de.faustedition.model.store;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
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
		for (PropertyIterator propertyIterator = node.getProperties(); propertyIterator.hasNext(); ) {
			Property property = propertyIterator.nextProperty();
			String propertyName = property.getName();
			try {
				toStringBuilder.append(propertyName, property.getValue().getString());
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

}
