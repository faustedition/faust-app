package de.faustedition.model.metadata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;

import de.faustedition.model.store.AbstractContentObject;
import de.faustedition.model.store.ContentObject;

public class MetadataBundle extends AbstractContentObject {
	public static final String NODE_NAME = "metadata";

	private Map<String, String> values = new HashMap<String, String>();

	protected MetadataBundle(String path) {
		super(path);
	}

	protected MetadataBundle(ContentObject annotated) {
		super(annotated, NODE_NAME);
	}

	public Map<String, String> getValues() {
		return values;
	}

	public void setValues(Map<String, String> values) {
		this.values = values;
	}

	public static Collection<MetadataBundle> find(Session session, ContentObject annotated) throws RepositoryException {
		Node annotatedNode = annotated.getNode(session);
		if (!annotatedNode.isNodeType("faust:annotated")) {
			return Collections.emptySet();
		}
		SortedSet<MetadataBundle> bundles = new TreeSet<MetadataBundle>();
		for (NodeIterator ni = annotatedNode.getNodes(NODE_NAME); ni.hasNext(); ){
			Node node = ni.nextNode();
			MetadataBundle metadataBundle = new MetadataBundle(node.getPath());
			for (PropertyIterator pi = node.getProperties(); pi.hasNext();) {
				Property property = pi.nextProperty();
				if (property.getName().startsWith("faust:")) {
					metadataBundle.getValues().put(StringUtils.removeStart(property.getName(), "faust:"), property.getString());
				}
			}
			bundles.add(metadataBundle);
		}
		return bundles;
	}

	public static MetadataBundle create(Session session, ContentObject annotated, Map<String, String> values) throws RepositoryException {
		MetadataBundle bundle = new MetadataBundle(annotated);
		bundle.setValues(values);
		bundle.save(annotated.getNode(session).addNode(NODE_NAME, "faust:metadata"));
		return bundle;
	}

	protected void save(Node node) throws RepositoryException {
		for (Map.Entry<String, String> metadata : values.entrySet()) {
			node.setProperty("faust:" + metadata.getKey(), metadata.getValue());
		}
	}

	public void update(Node node) throws RepositoryException {
		for (PropertyIterator pi = node.getProperties(); pi.hasNext();) {
			Property property = pi.nextProperty();
			if (property.getName().startsWith("faust:")) {
				property.remove();
			}
		}
		save(node);
	}
}
