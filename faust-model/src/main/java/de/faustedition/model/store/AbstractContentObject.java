package de.faustedition.model.store;


import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

public class AbstractContentObject implements ContentObject {

	protected String path;

	protected AbstractContentObject(String path) {
		this.path = ContentStore.normalizePath(path);
	}

	protected AbstractContentObject(ContentObject parent, String name) {
		this(parent.getPath() + "/" + name);
		assert ContentStore.isValidName(name);
	}

	@Override
	public String getName() {
		return StringUtils.substringAfterLast(this.path, "/");
	}

	@Override
	public String getPath() {
		return path;
	}
 
	public Node getNode(Session session) throws RepositoryException {
		return session.getRootNode().getNode(getPath());
	}

	@Override
	public int compareTo(ContentObject o) {
		return path.compareTo(o.getPath());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("path", getPath()).toString();
	}
	
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
}
