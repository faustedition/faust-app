package de.faustedition.paper;

import de.faustedition.document.DocumentUnit;

public class PaperProperties {
	private DocumentUnit documentUnit;

	/**
	 * Hersteller.
	 */
	private String producer;

	/**
	 * Papiersorte.
	 */
	private String grade;

	/**
	 * Papierart.
	 */
	private String type;

	/**
	 * Papierformat.
	 */
	private String format;

	public DocumentUnit getDocumentUnit() {
		return documentUnit;
	}

	public void setDocumentUnit(DocumentUnit documentUnit) {
		this.documentUnit = documentUnit;
	}

	public String getProducer() {
		return producer;
	}

	public void setProducer(String producer) {
		this.producer = producer;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
}
