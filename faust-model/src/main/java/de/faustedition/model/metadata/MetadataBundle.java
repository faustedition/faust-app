package de.faustedition.model.metadata;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;

import de.faustedition.model.repository.DataRepository;
import de.faustedition.model.repository.RepositoryObject;
import de.faustedition.model.repository.RepositoryObjectBase;

public class MetadataBundle extends RepositoryObjectBase {
	public static final String NODE_NAME = "metadata";

	private Map<String, MetadataValue> values;

	public MetadataBundle(String path) {
		super(path);
	}

	protected MetadataBundle(String path, Map<String, MetadataValue> values) {
		this(path);
		this.values = values;
	}

	public Map<String, MetadataValue> getValues() {
		return values;
	}

	public static MetadataBundle getAggregatedMetadata(Session session, RepositoryObject annotated) throws RepositoryException {
		Map<String, MetadataValue> aggregated = new HashMap<String, MetadataValue>();
		
		for (MetadataBundle bundle : annotated.find(session, MetadataBundle.class)) {
			for (MetadataValue value : bundle.getValues().values()) {
				String field = value.getField();
				aggregated.put(field, aggregated.containsKey(field) ? value.aggregate(aggregated.get(field)) : value);
			}
		}
		
		return new MetadataBundle(DataRepository.concatenatePath(annotated, NODE_NAME), aggregated);
	}
	
	public SortedMap<MetadataFieldGroup, SortedSet<MetadataValue>> getStructuredMetadata() {
		SortedMap<MetadataFieldGroup, SortedSet<MetadataValue>> metadataStructure = new TreeMap<MetadataFieldGroup, SortedSet<MetadataValue>>();
		for (MetadataValue value : values.values()) {
			MetadataFieldGroup fieldGroup = value.getDefinition().getGroup();
			if (metadataStructure.containsKey(fieldGroup)) {
				metadataStructure.get(fieldGroup).add(value);
			} else {
				SortedSet<MetadataValue> valueSet = new TreeSet<MetadataValue>(new Comparator<MetadataValue>() {

					@Override
					public int compare(MetadataValue o1, MetadataValue o2) {
						return o1.getDefinition().compareTo(o2.getDefinition());
					}
				});
				valueSet.add(value);
				metadataStructure.put(fieldGroup, valueSet);
			}
		}
		return metadataStructure;
	}

	public static MetadataBundle create(Session session, RepositoryObject annotated, Map<String, MetadataValue> values) throws RepositoryException {
		MetadataBundle bundle = new MetadataBundle(annotated.getPath(), values);
		bundle.save(annotated.getNode(session).addNode(NODE_NAME, "faust:metadata"));
		return bundle;
	}

	protected void save(Node node) throws RepositoryException {
		for (MetadataValue metadata : values.values()) {
			node.setProperty("faust:" + metadata.getField(), metadata.getValue());
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

	@Override
	public void load(Node node) throws RepositoryException {
		if (!node.getName().equals(NODE_NAME) || !node.getParent().isNodeType("faust:annotated")) {
			throw new IllegalArgumentException();
		}

		values = new HashMap<String, MetadataValue>();
		for (PropertyIterator pi = node.getProperties(); pi.hasNext();) {
			Property property = pi.nextProperty();
			if (property.getName().startsWith("faust:")) {
				String field = StringUtils.removeStart(property.getName(), "faust:");
				values.put(field, new MetadataValue(field, property.getString()));
			}
		}
	}
}
