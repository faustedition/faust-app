package de.faustedition.model.service;

import java.util.List;

import de.faustedition.model.HierarchyNode;
import de.faustedition.model.metadata.MetadataValue;

public interface MetadataManager {
	void init() throws Exception;

	void clear();

	MetadataValue createValue(HierarchyNode node, String field, String value);

	List<MetadataValue> findMetadataValues(HierarchyNode node);
}
