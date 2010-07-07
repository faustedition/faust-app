package de.faustedition.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

@Configurable
public class DocumentUnit {
	@Autowired
	protected SimpleJdbcTemplate jt;

	private Integer nestedSetLeft;
	private Integer nestedSetRight;
	private DocumentUnitType type;

	public Integer getNestedSetLeft() {
		return nestedSetLeft;
	}

	public void setNestedSetLeft(Integer nestedSetLeft) {
		this.nestedSetLeft = nestedSetLeft;
	}

	public Integer getNestedSetRight() {
		return nestedSetRight;
	}

	public void setNestedSetRight(Integer nestedSetRight) {
		this.nestedSetRight = nestedSetRight;
	}

	public DocumentUnitType getType() {
		return type;
	}

	public void setType(DocumentUnitType type) {
		this.type = type;
	}
}
