package de.faustedition.model.metadata;

import de.faustedition.model.Model;

public class MetadataFieldGroup extends Model {
	private String name;
	private int groupOrder;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getGroupOrder() {
		return groupOrder;
	}

	public void setGroupOrder(int groupOrder) {
		this.groupOrder = groupOrder;
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj != null) && getClass().equals(obj.getClass())) {
			return this.name.equalsIgnoreCase(((MetadataFieldGroup) obj).name);
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return this.name.toUpperCase().hashCode();
	}
}
