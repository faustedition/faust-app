package de.faustedition.model.repository;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

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
import org.compass.core.Resource;
import org.compass.core.ResourceFactory;

import de.faustedition.model.metadata.MetadataBundle;
import de.faustedition.model.metadata.MetadataValue;
import de.faustedition.util.ErrorUtil;

public abstract class RepositoryObjectBase implements RepositoryObject {

	protected String path;

	protected RepositoryObjectBase(String path) {
		this.path = DataRepository.normalizePath(path);
	}

	@Override
	public String getName() {
		return StringUtils.substringAfterLast(this.path, "/");
	}

	public static <T extends RepositoryObject> T get(Class<T> type, Node node) throws RepositoryException {
		try {
			T object = type.getConstructor(String.class).newInstance(node.getPath());
			object.load(node);
			return object;
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (RepositoryException e) {
			throw e;
		} catch (Exception e) {
			throw ErrorUtil.fatal(String.format("Error instantiating repository object '%s' of type %s", node.getPath(), type.getCanonicalName()), e);
		}
	}

	public <T extends RepositoryObject> SortedSet<T> find(Session session, Class<T> type) throws RepositoryException {
		SortedSet<T> childObjects = new TreeSet<T>();
		for (NodeIterator ni = getNode(session).getNodes(); ni.hasNext();) {
			try {
				childObjects.add(get(type, ni.nextNode()));
			} catch (IllegalArgumentException e) {
			}
		}
		return childObjects;
	}

	public <T extends RepositoryObject> T get(Session session, Class<T> type, String name) throws RepositoryException {
		return get(type, getNode(session).getNode(name));
	}

	@Override
	public String getPath() {
		return path;
	}

	public Node getNode(Session session) throws RepositoryException {
		return session.getRootNode().getNode(getPath());
	}

	public Resource getMetadataResource(ResourceFactory factory, Session session) throws RepositoryException {
		Map<String, MetadataValue> metadata = MetadataBundle.getAggregatedMetadata(session, this).getValues();
		if (metadata.isEmpty()) {
			return null;
		}

		Resource metadataResource = factory.createResource("metadata");
		metadataResource.addProperty("path", getPath());
		metadataResource.addProperty("repositoryType", getClass().getName());
		for (String metadataField : metadata.keySet()) {
			metadataResource.addProperty(metadataField, metadata.get(metadataField).getValue());
		}
		return metadataResource;
	}

	@SuppressWarnings("unchecked")
	public static RepositoryObject createFromMetadataResource(Resource resource, Session session) throws RepositoryException {
		try {
			Class<? extends RepositoryObject> type = (Class<? extends RepositoryObject>) Class.forName(resource.getValue("repositoryType"));
			Node node = session.getRootNode().getNode(resource.getValue("path"));
			return get(type, node);
		} catch (ClassNotFoundException e) {
			throw ErrorUtil.fatal("Cannot create repository object from search index", e);
		}
	}

	@Override
	public int compareTo(RepositoryObject o) {
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
