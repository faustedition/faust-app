package de.faustedition.model.metadata;

import de.faustedition.model.HierarchyNodeType;
import de.faustedition.model.Model;

public class MetadataField extends Model {
	private MetadataFieldGroup group;
	private String name;
	private int fieldOrder;
	private HierarchyNodeType lowestLevel;
	private MetadataFieldType type;

	public MetadataFieldGroup getGroup() {
		return group;
	}

	public void setGroup(MetadataFieldGroup group) {
		this.group = group;
	}

	public Integer getGroupId() {
		return (this.group == null ? null : this.group.getId());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getFieldOrder() {
		return fieldOrder;
	}

	public void setFieldOrder(int fieldOrder) {
		this.fieldOrder = fieldOrder;
	}

	public HierarchyNodeType getLowestLevel() {
		return lowestLevel;
	}

	public void setLowestLevel(HierarchyNodeType lowestLevel) {
		this.lowestLevel = lowestLevel;
	}

	public String getLowestLevelValue() {
		return (this.lowestLevel == null ? null : this.lowestLevel.toString());
	}

	public void setLowestLevelValue(String value) {
		this.lowestLevel = (value == null ? null : HierarchyNodeType.valueOf(value));
	}

	public MetadataFieldType getType() {
		return type;
	}

	public void setType(MetadataFieldType type) {
		this.type = type;
	}

	public String getTypeValue() {
		return (this.type == null ? null : type.toString());
	}

	public void setTypeValue(String value) {
		this.type = (value == null ? null : MetadataFieldType.valueOf(value));
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj != null) && getClass().equals(obj.getClass())) {
			return this.name.equalsIgnoreCase(((MetadataField) obj).name);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return this.name.toUpperCase().hashCode();
	}
}
