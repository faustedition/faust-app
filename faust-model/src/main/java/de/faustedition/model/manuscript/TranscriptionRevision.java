package de.faustedition.model.manuscript;

public class TranscriptionRevision {
	private String author;
	private String date;
	private String description;

	public TranscriptionRevision() {
	}

	public TranscriptionRevision(String author, String date, String description) {
		this.author = author;
		this.date = date;
		this.description = description;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
