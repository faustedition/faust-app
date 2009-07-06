package de.faustedition.model.metadata;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

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

	public Integer getNodeId() {
		return (this.node == null ? null : this.node.getId());
	}

	public MetadataField getField() {
		return field;
	}

	public void setField(MetadataField field) {
		this.field = field;
	}

	public Integer getFieldId() {
		return (this.field == null ? null : this.field.getId());
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
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
}
