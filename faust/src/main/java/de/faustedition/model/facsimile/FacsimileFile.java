package de.faustedition.model.facsimile;

import org.hibernate.Session;

public class FacsimileFile {
	private long id;
	private String path;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj != null) && (obj instanceof FacsimileFile)) {
			return path.equals(((FacsimileFile) obj).path);
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}

	public void save(Session session) {
		session.saveOrUpdate(this);
	}

}
