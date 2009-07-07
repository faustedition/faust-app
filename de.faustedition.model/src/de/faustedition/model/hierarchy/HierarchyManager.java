package de.faustedition.model.hierarchy;

import java.util.List;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;


public interface HierarchyManager {
	final String NODE_SELECT_LIST = "n.id AS n_id, n.created AS n_created, n.last_modified AS n_last_modified, n.name AS n_name, n.path AS n_path, n.node_type_value AS n_node_type_value, n.tree_left AS n_tree_left, n.tree_right AS n_tree_right, n.tree_level AS n_tree_level";

	HierarchyNode findRoot();

	void init() throws Exception;

	HierarchyNode loadNode(int id);

	boolean nodeExists(HierarchyNode parent, String name);

	HierarchyNode findNode(HierarchyNode parent, String name);

	HierarchyNode createNode(HierarchyNode parent, String name, HierarchyNodeType type);

	void clear();

	HierarchyNode loadByPath(String nodePath);

	List<HierarchyNode> findChildren(HierarchyNode node);

	ParameterizedRowMapper<HierarchyNode> getNodeMapper();
}
