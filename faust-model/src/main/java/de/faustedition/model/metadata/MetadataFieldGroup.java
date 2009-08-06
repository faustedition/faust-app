package de.faustedition.model.metadata;

import de.faustedition.model.Model;

public class MetadataFieldGroup extends Model {
	private String name;
	private int order;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
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
