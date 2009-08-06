package de.faustedition.model.metadata;

import java.util.LinkedHashMap;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.QName;

import de.faustedition.model.TEIDocument;

public class ManuscriptIdentifier {
	private String institution;

	private String repository;

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
	
	public void set(TEIDocument teiDocument) {
		Element msIdentifierElement = teiDocument.makeElement("teiHeader/fileDesc/sourceDesc/msDesc/msIdentifier");
		TEIDocument.setPropertyElement(msIdentifierElement, "institution", institution);
		TEIDocument.setPropertyElement(msIdentifierElement, "repository", repository);
		
		msIdentifierElement.elements(TEIDocument.teiName("idno")).clear();
		QName typeAttributeName = TEIDocument.teiName("type");
		for (Map.Entry<String, String> identifier : identifiers.entrySet()) {
			TEIDocument.setPropertyElement(msIdentifierElement, "idno", identifier.getValue()).addAttribute(typeAttributeName, identifier.getKey());
		}
	}
}
