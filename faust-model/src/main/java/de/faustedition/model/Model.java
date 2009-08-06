package de.faustedition.model;

import java.util.Date;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Session;

public class Model {
	private int id = 0;
	private int version = -1;
	private Date created = new Date();
	private Date lastModified;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
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

	@SuppressWarnings("unchecked")
	public static <T extends Model> T load(Session session, Class<T> type, int id) {
		return (T) session.load(type, id);
	}

	public void save(Session session) {
		if (this.id != 0) {
			setLastModified(new Date());
		}
		session.saveOrUpdate(this);
	}

	public void remove(Session session) {
		assert (this.id != 0);
		session.delete(this);
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
