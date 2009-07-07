package de.faustedition.model.transcription;

import de.faustedition.model.Model;

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
