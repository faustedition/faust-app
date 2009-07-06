package de.faustedition.model;

import java.sql.Date;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Model {
	private int id = 0;
	private Date created = new Date(System.currentTimeMillis());
	private Date lastModified;

	public Model() {
	}

	public Model(Model model) {
		this.id = model.id;
		this.created = model.created;
		this.lastModified = model.lastModified;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public boolean equals(Object obj) {
		if ((this.id != 0) && (obj != null) && getClass().equals(obj.getClass())) {
			Model other = (Model) obj;
			if (other.id != 0) {
				return this.id == other.id;
			}
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return (this.id == 0 ? super.hashCode() : new HashCodeBuilder().append(this.id).toHashCode());
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
