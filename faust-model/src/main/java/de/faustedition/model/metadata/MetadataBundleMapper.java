package de.faustedition.model.metadata;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

import de.faustedition.model.store.ContentObjectMapper;
import de.faustedition.model.store.ContentStore;
import de.faustedition.model.store.ContentStoreUtil;

public class MetadataBundleMapper implements ContentObjectMapper<MetadataBundle> {

	private static final Object METADATA_NODE_NAME = "metadata";

	@Override
	public MetadataBundle map(Node node) throws RepositoryException {
		MetadataBundle metadataBundle = new MetadataBundle(ContentStoreUtil.normalizePath(node.getPath()), node.getName());
		for (PropertyIterator pi = node.getProperties(); pi.hasNext();) {
			Property property = pi.nextProperty();
			String namePrefix = ContentStore.FAUST_NS_PREFIX + ":";
			if (property.getName().startsWith(namePrefix)) {
				metadataBundle.getValues().put(StringUtils.removeStart(property.getName(), namePrefix), property.getString());
			}
		}
		return metadataBundle;
	}

	@Override
	public boolean mapsObjectFor(Node node) throws RepositoryException {
		return node.getName().equals(METADATA_NODE_NAME) && node.getParent().isNodeType("faust:annotated");
	}

	@Override
	public void save(MetadataBundle contentObject, Node node) throws RepositoryException {
		for (PropertyIterator pi = node.getProperties(); pi.hasNext();) {
			Property property = pi.nextProperty();
			String namePrefix = ContentStore.FAUST_NS_PREFIX + ":";
			if (property.getName().startsWith(namePrefix)) {
				property.remove();
			}
		}
		for (Map.Entry<String, String> metadata : contentObject.getValues().entrySet()) {
			node.setProperty(ContentStore.FAUST_NS_PREFIX + ":" + metadata.getKey(), metadata.getValue());
		}
	}

	@Override
	public Class<? extends MetadataBundle> getMappedType() {
		return MetadataBundle.class;
	}
}
