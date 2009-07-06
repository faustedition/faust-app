package de.faustedition.model.metadata;

import java.util.LinkedHashMap;
import java.util.Map;

public class ManuscriptIdentifier {
	private String institution;

	private String repository;

	private String collection;

	private Map<String, String> identifiers = new LinkedHashMap<String, String>();

	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public Map<String, String> getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(Map<String, String> identifiers) {
		this.identifiers = identifiers;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public String getCollection() {
		return collection;
	}
}
