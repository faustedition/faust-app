package de.faustedition.model.service;

import java.util.List;

import de.faustedition.model.HierarchyNode;
import de.faustedition.model.HierarchyNodeType;

public interface HierarchyManager {
	HierarchyNode findRoot();

	void initHierarchy();

	HierarchyNode loadNode(int id);

	boolean nodeExists(HierarchyNode parent, String name);

	HierarchyNode findNode(HierarchyNode parent, String name);

	HierarchyNode createNode(HierarchyNode parent, String name, HierarchyNodeType type);

	void clear();

	HierarchyNode loadByPath(String nodePath);

	List<HierarchyNode> findChildren(HierarchyNode node);
}
