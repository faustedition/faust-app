package de.faustedition.model.metadata;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import de.faustedition.model.HierarchyNode;
import de.faustedition.model.Model;

public class MetadataValue extends Model {
	private HierarchyNode node;
	private MetadataField field;
	private String value;

	public MetadataValue() {
		super();
	}

	public MetadataValue(HierarchyNode node, MetadataField field, String value) {
		super();
		setNode(node);
		setField(field);
		setValue(value);
	}

	public HierarchyNode getNode() {
		return node;
	}

	public void setNode(HierarchyNode node) {
		this.node = node;
	}

	public MetadataField getField() {
		return field;
	}

	public void setField(MetadataField field) {
		this.field = field;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public static void clear(Session session) {
		session.createQuery("DELETE MetadataValue").executeUpdate();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && getClass().equals(obj.getClass())) {
			MetadataValue other = (MetadataValue) obj;
			return new EqualsBuilder().append(this.node, other.node).append(this.field, other.field).isEquals();
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.node).append(this.field).toHashCode();
	}

	@SuppressWarnings("unchecked")
	public static List<MetadataValue> findByNode(Session session, HierarchyNode node) {
		return createValueCriteria(session).createCriteria("node").add(Restrictions.idEq(node.getId())).list();
	}


	protected static Criteria createValueCriteria(Session session) {
		Criteria valueCriteria = session.createCriteria(MetadataValue.class);
		valueCriteria.setFetchMode("node", FetchMode.JOIN);
		valueCriteria.setFetchMode("field", FetchMode.JOIN);
		valueCriteria.setFetchMode("field.group", FetchMode.JOIN);
		return valueCriteria;
	}
}
