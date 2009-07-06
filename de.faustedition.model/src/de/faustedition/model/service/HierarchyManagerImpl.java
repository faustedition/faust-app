package de.faustedition.model.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.faustedition.model.HierarchyNode;
import de.faustedition.model.HierarchyNodeType;
import de.faustedition.util.DatabaseUtil;

@Service("hierarchyManager")
@Transactional(readOnly = true)
public class HierarchyManagerImpl implements HierarchyManager {
	private static final String CHILD_NODES_CLAUSES = "n.tree_left > :treeLeft AND n.tree_right < :treeRight AND n.tree_level = (:treeLevel + 1)";

	private DataSource dataSource;
	private SimpleJdbcTemplate jdbcTemplate;
	private SimpleJdbcInsert nodeInsert;
	private HierarchyNodeMapper nodeMapper = new HierarchyNodeMapper();

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
		this.nodeInsert = new SimpleJdbcInsert(dataSource).withTableName("hierarchy_node").usingGeneratedKeyColumns("id");
	}

	public ParameterizedRowMapper<HierarchyNode> getNodeMapper() {
		return this.nodeMapper;
	}

	public HierarchyNode findRoot() {
		return (HierarchyNode) DataAccessUtils.requiredUniqueResult(jdbcTemplate.query("SELECT " + NODE_SELECT_LIST
				+ " FROM hierarchy_node n WHERE n.tree_level = 0", nodeMapper));
	}

	@Transactional(readOnly = false)
	public void init() throws Exception {
		if (!DatabaseUtil.tableExists(dataSource, "hierarchy_node")) {
			jdbcTemplate.getJdbcOperations().execute(
					"CREATE TABLE hierarchy_node "
							+ "(id SERIAL PRIMARY KEY, created TIMESTAMP NOT NULL, last_modified TIMESTAMP, "
							+ "name VARCHAR(100) NOT NULL, path VARCHAR(256) NOT NULL, node_type_value VARCHAR(32) NOT NULL, "
							+ "tree_level INTEGER NOT NULL, tree_left INTEGER NOT NULL, tree_right INTEGER NOT NULL)");
		}
		try {
			findRoot();
		} catch (IncorrectResultSizeDataAccessException e) {
			HierarchyNode root = HierarchyNode.createRoot();
			root.setId(this.nodeInsert.executeAndReturnKey(new BeanPropertySqlParameterSource(root)).intValue());
		}
	}

	@Transactional(readOnly = false)
	public void clear() {
		jdbcTemplate.update("DELETE FROM hierarchy_node WHERE tree_level > 0");
	}

	@Transactional(readOnly = false)
	public HierarchyNode createNode(HierarchyNode parent, String name, HierarchyNodeType type) {
		if (nodeExists(parent, name)) {
			throw new DataIntegrityViolationException(String.format("'%s' already exists in '%s'", name, parent.getFullPath()));
		}

		jdbcTemplate.update("UPDATE hierarchy_node SET tree_left = tree_left + 2 WHERE tree_left > ?", parent.getTreeLeft());
		jdbcTemplate.update("UPDATE hierarchy_node SET tree_right = tree_right + 2 WHERE tree_right >= ?", parent.getTreeRight());

		HierarchyNode child = new HierarchyNode();
		child.setName(name);
		child.setNodeType(type);
		child.setPath(parent.getFullPath());
		child.setTreeLeft(parent.getTreeRight());
		child.setTreeRight(parent.getTreeRight() + 1);
		child.setTreeLevel(parent.getTreeLevel() + 1);
		child.setId(nodeInsert.executeAndReturnKey(new BeanPropertySqlParameterSource(child)).intValue());

		parent.setTreeRight(parent.getTreeRight() + 2);

		return child;
	}

	public boolean nodeExists(HierarchyNode parent, String name) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("treeLeft", parent.getTreeLeft());
		parameters.put("treeRight", parent.getTreeRight());
		parameters.put("treeLevel", parent.getTreeLevel());
		parameters.put("name", HierarchyNode.escapeName(name));

		return jdbcTemplate.queryForInt("SELECT COUNT(n.id) FROM hierarchy_node n WHERE " + CHILD_NODES_CLAUSES
				+ " AND n.name = :name", new MapSqlParameterSource(parameters)) > 0;
	}

	public HierarchyNode findNode(HierarchyNode parent, String name) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("treeLeft", parent.getTreeLeft());
		parameters.put("treeRight", parent.getTreeRight());
		parameters.put("treeLevel", parent.getTreeLevel());
		parameters.put("name", HierarchyNode.escapeName(name));

		return (HierarchyNode) DataAccessUtils.singleResult(jdbcTemplate.query("SELECT " + NODE_SELECT_LIST
				+ " FROM hierarchy_node n WHERE " + CHILD_NODES_CLAUSES + " AND n.name = :name", nodeMapper,
				new MapSqlParameterSource(parameters)));
	}

	public HierarchyNode loadNode(int id) {
		return (HierarchyNode) DataAccessUtils.requiredUniqueResult(jdbcTemplate.query("SELECT " + NODE_SELECT_LIST
				+ " FROM hierarchy_node n WHERE n.id = ?", nodeMapper, id));
	}

	public HierarchyNode loadByPath(String fullPath) {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("path", (fullPath.contains("/") ? StringUtils.substringBeforeLast(fullPath, "/") : ""));
		parameters.put("name", (fullPath.contains("/") ? StringUtils.substringAfterLast(fullPath, "/") : fullPath));

		return (HierarchyNode) DataAccessUtils.requiredUniqueResult(jdbcTemplate.query("SELECT " + NODE_SELECT_LIST
				+ " FROM hierarchy_node n WHERE n.path = :path AND n.name = :name", nodeMapper, new MapSqlParameterSource(
				parameters)));
	}

	public List<HierarchyNode> findChildren(HierarchyNode node) {
		return jdbcTemplate.query("SELECT " + NODE_SELECT_LIST + " FROM hierarchy_node n WHERE " + CHILD_NODES_CLAUSES
				+ " ORDER BY n.name", nodeMapper, new BeanPropertySqlParameterSource(node));
	}

	private class HierarchyNodeMapper implements ParameterizedRowMapper<HierarchyNode> {

		public HierarchyNode mapRow(ResultSet rs, int row) throws SQLException {
			HierarchyNode node = new HierarchyNode();
			node.setId(rs.getInt("n_id"));
			node.setCreated(rs.getDate("n_created"));
			node.setLastModified(rs.getDate("n_last_modified"));
			node.setName(rs.getString("n_name"));
			node.setNodeTypeValue(rs.getString("n_node_type_value"));
			node.setPath(rs.getString("n_path"));
			node.setTreeLeft(rs.getInt("n_tree_left"));
			node.setTreeLevel(rs.getInt("n_tree_level"));
			node.setTreeRight(rs.getInt("n_tree_right"));
			return node;
		}

	}
}
