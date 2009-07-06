package de.faustedition.model.service;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.faustedition.model.HierarchyNode;
import de.faustedition.model.HierarchyNodeType;
import de.faustedition.model.metadata.MetadataField;
import de.faustedition.model.metadata.MetadataFieldGroup;
import de.faustedition.model.metadata.MetadataFieldType;
import de.faustedition.model.metadata.MetadataValue;
import de.faustedition.util.DatabaseUtil;
import de.faustedition.util.ErrorUtil;
import de.faustedition.util.LoggingUtil;
import de.faustedition.util.XMLUtil;

@Service("metadataManager")
@Transactional(readOnly = true)
public class MetadataManagerImpl implements MetadataManager {
	private static final String FIELD_SELECT_LIST = "f.id AS f_id, f.created AS f_created, f.last_modified AS f_last_modified, f.name AS f_name, f.field_order AS f_field_order, f.lowest_level_value AS f_lowest_level_value, f.type_value AS f_type_value";
	private static final String FIELD_GROUP_SELECT_LIST = "fg.id AS fg_id, fg.created AS fg_created, fg.last_modified AS fg_last_modified, fg.name AS fg_name, fg.group_order AS fg_group_order";
	private static final String VALUE_SELECT_LIST = "mv.id AS mv_id, mv.created AS mv_created, mv.last_modified AS mv_last_modified, mv.value AS mv_value";
	private static final Resource METADATA_FIELD_DEFINITIONS = new ClassPathResource("/metadata-field-definition.xml");

	@Autowired
	private HierarchyManager hierarchyManager;

	private DataSource dataSource;
	private SimpleJdbcTemplate jdbcTemplate;
	private SimpleJdbcInsert groupInsert;
	private SimpleJdbcInsert fieldInsert;
	private SimpleJdbcInsert valueInsert;
	private MetadataFieldGroupMapper fieldGroupMapper = new MetadataFieldGroupMapper();
	private MetadataFieldMapper fieldMapper = new MetadataFieldMapper();
	private MetadataValueMapper metadataValueMapper = new MetadataValueMapper();

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
		this.groupInsert = new SimpleJdbcInsert(dataSource).withTableName("metadata_field_group").usingGeneratedKeyColumns("id");
		this.fieldInsert = new SimpleJdbcInsert(dataSource).withTableName("metadata_field").usingGeneratedKeyColumns("id");
		this.valueInsert = new SimpleJdbcInsert(dataSource).withTableName("metadata_value").usingGeneratedKeyColumns("id");
	}

	@Transactional(readOnly = false)
	public void init() throws Exception {
		initDatabaseSchema();
		initMetadataSchema();
	}

	@Transactional(readOnly = false)
	public void clear() {
		jdbcTemplate.update("DELETE FROM metadata_value");
	}

	public MetadataValue createValue(HierarchyNode node, String field, String value) {
		MetadataField metadataField = (MetadataField) DataAccessUtils.requiredSingleResult(jdbcTemplate.query("SELECT "
				+ FIELD_SELECT_LIST + ", " + FIELD_GROUP_SELECT_LIST + " FROM metadata_field f "
				+ "JOIN metadata_field_group fg ON (f.group_id = fg.id) WHERE f.name = ?", fieldMapper, field));
		MetadataValue metadataValue = new MetadataValue(node, metadataField, value);
		metadataValue.setId(valueInsert.executeAndReturnKey(new BeanPropertySqlParameterSource(metadataValue)).intValue());
		return metadataValue;
	}

	public List<MetadataValue> findMetadataValues(HierarchyNode node) {
		return jdbcTemplate.query("SELECT " + FIELD_SELECT_LIST + ", " + FIELD_GROUP_SELECT_LIST + ", " + VALUE_SELECT_LIST + ", "
				+ HierarchyManager.NODE_SELECT_LIST + " FROM metadata_value mv JOIN hierarchy_node n ON (mv.node_id = n.id) "
				+ "JOIN metadata_field f ON (mv.field_id = f.id) JOIN metadata_field_group fg ON (f.group_id = fg.id) "
				+ "WHERE n.id = ?", metadataValueMapper, node.getId());
	}

	private void initDatabaseSchema() throws Exception {
		if (!DatabaseUtil.tableExists(dataSource, "metadata_field_group")) {
			jdbcTemplate.getJdbcOperations().execute(
					"CREATE TABLE metadata_field_group "
							+ "(id SERIAL PRIMARY KEY, created TIMESTAMP NOT NULL, last_modified TIMESTAMP, "
							+ "name VARCHAR(100) NOT NULL, group_order INTEGER NOT NULL)");
		}
		if (!DatabaseUtil.tableExists(dataSource, "metadata_field")) {
			jdbcTemplate.getJdbcOperations().execute(
					"CREATE TABLE metadata_field "
							+ "(id SERIAL PRIMARY KEY, created TIMESTAMP NOT NULL, last_modified TIMESTAMP, "
							+ "group_id INTEGER REFERENCES metadata_field_group (id), name VARCHAR(100) NOT NULL, "
							+ "field_order INTEGER NOT NULL, lowest_level_value VARCHAR(32) NOT NULL, "
							+ "type_value VARCHAR(32) NOT NULL)");
		}
		if (!DatabaseUtil.tableExists(dataSource, "metadata_value")) {
			jdbcTemplate.getJdbcOperations().execute(
					"CREATE TABLE metadata_value "
							+ "(id SERIAL PRIMARY KEY, created TIMESTAMP NOT NULL, last_modified TIMESTAMP, "
							+ "node_id INTEGER REFERENCES hierarchy_node (id), field_id INTEGER REFERENCES metadata_field (id), "
							+ "value TEXT)");
		}
	}

	private void initMetadataSchema() {
		if (jdbcTemplate.queryForInt("SELECT COUNT(*) FROM metadata_field") > 0) {
			return;
		}

		LoggingUtil.log(Level.INFO, String.format("Importing metadata field definitions from %s", METADATA_FIELD_DEFINITIONS
				.getDescription()));
		try {
			XMLUtil.parse(METADATA_FIELD_DEFINITIONS.getInputStream(), new DefaultHandler() {

				StringBuilder characterBuf;
				MetadataFieldGroup currentFieldGroup;
				MetadataField currentField;
				int fieldOrder = 0;

				@Override
				public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
					if ("field".equals(name)) {
						currentField = new MetadataField();
						currentField.setFieldOrder(fieldOrder++);
					} else if ("field-group".equals(name)) {
						MetadataFieldGroup newFieldGroup = new MetadataFieldGroup();
						if (attributes.getIndex("name") < 0) {
							throw new SAXException("<field-group/> without name attribute");
						}
						newFieldGroup.setName(attributes.getValue("name").trim());
						if (newFieldGroup.getName().length() == 0) {
							throw new SAXException("<field-group/> with empty name attribute");
						}
						if (currentFieldGroup != null) {
							newFieldGroup.setGroupOrder(currentFieldGroup.getGroupOrder() + 1);
						}

						currentFieldGroup = newFieldGroup;
						currentFieldGroup.setId(groupInsert.executeAndReturnKey(
								new BeanPropertySqlParameterSource(currentFieldGroup)).intValue());
						fieldOrder = 0;
					} else {
						characterBuf = new StringBuilder();
					}
				}

				@Override
				public void endElement(String uri, String localName, String name) throws SAXException {
					if ("field".equals(name)) {
						currentField.setGroup(currentFieldGroup);
						fieldInsert.execute(new BeanPropertySqlParameterSource(currentField));
						currentField = null;
					} else if (currentField != null) {
						String value = StringUtils.trimToNull(characterBuf.toString());
						characterBuf = null;
						if (value == null) {
							return;
						}

						if ("name".equals(name)) {
							currentField.setName(value);
						} else if ("type".equals(name)) {
							currentField.setType(MetadataFieldType.valueOf(value));
						} else if ("lowestLevel".equals(name)) {
							currentField.setLowestLevel(HierarchyNodeType.valueOf(value));
						}
					}
				}

				@Override
				public void characters(char[] ch, int start, int length) throws SAXException {
					if (characterBuf != null) {
						characterBuf.append(ch, start, length);
					}
				}
			});
		} catch (SAXException e) {
			throw ErrorUtil.fatal("XML parser error while reading metadata field definitions", e);
		} catch (IOException e) {
			throw ErrorUtil.fatal("I/O error while reading metadata field definitions", e);
		}
	}

	private class MetadataFieldGroupMapper implements ParameterizedRowMapper<MetadataFieldGroup> {

		public MetadataFieldGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
			MetadataFieldGroup fieldGroup = new MetadataFieldGroup();
			fieldGroup.setId(rs.getInt("fg_id"));
			fieldGroup.setCreated(rs.getDate("fg_created"));
			fieldGroup.setLastModified(rs.getDate("fg_last_modified"));
			fieldGroup.setName(rs.getString("fg_name"));
			fieldGroup.setGroupOrder(rs.getInt("fg_group_order"));

			return fieldGroup;
		}

	}

	private class MetadataFieldMapper implements ParameterizedRowMapper<MetadataField> {

		public MetadataField mapRow(ResultSet rs, int rowNum) throws SQLException {
			MetadataField field = new MetadataField();
			field.setId(rs.getInt("f_id"));
			field.setCreated(rs.getDate("f_created"));
			field.setLastModified(rs.getDate("f_last_modified"));
			field.setGroup(fieldGroupMapper.mapRow(rs, rowNum));
			field.setLowestLevel(HierarchyNodeType.valueOf(rs.getString("f_lowest_level_value")));
			field.setName(rs.getString("f_name"));
			field.setFieldOrder(rs.getInt("f_field_order"));
			field.setType(MetadataFieldType.valueOf(rs.getString("f_type_value")));

			return field;
		}
	}

	private class MetadataValueMapper implements ParameterizedRowMapper<MetadataValue> {

		public MetadataValue mapRow(ResultSet rs, int rowNum) throws SQLException {
			MetadataValue metadataValue = new MetadataValue();

			metadataValue.setId(rs.getInt("mv_id"));
			metadataValue.setCreated(rs.getDate("mv_created"));
			metadataValue.setLastModified(rs.getDate("mv_last_modified"));
			metadataValue.setField(fieldMapper.mapRow(rs, rowNum));
			metadataValue.setNode(hierarchyManager.getNodeMapper().mapRow(rs, rowNum));
			metadataValue.setValue(rs.getString("mv_value"));

			return metadataValue;
		}

	}
}
