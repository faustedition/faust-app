package de.faustedition.model;

public class Facsimile extends Model {
	private String path;

	public Facsimile(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
