package de.faustedition.model.metadata;

import java.util.List;

import de.faustedition.model.hierarchy.HierarchyNode;

public interface MetadataManager {
	void init() throws Exception;

	void clear();

	MetadataValue createValue(HierarchyNode node, String field, String value);

	List<MetadataValue> findMetadataValues(HierarchyNode node);
}
