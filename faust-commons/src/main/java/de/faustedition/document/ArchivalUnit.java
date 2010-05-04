package de.faustedition.document;

import com.google.common.base.Objects;

public class ArchivalUnit extends DocumentUnit {
	private Integer archiveId;
	private String unitId;
	private String callNumber;

	public Integer getArchiveId() {
		return archiveId;
	}

	public void setArchiveId(Integer archiveId) {
		this.archiveId = archiveId;
	}

	public String getUnitId() {
		return unitId;
	}

	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}

	public String getCallNumber() {
		return callNumber;
	}

	public void setCallNumber(String callNumber) {
		this.callNumber = callNumber;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ArchivalUnit) {
			ArchivalUnit au = (ArchivalUnit) obj;
			return Objects.equal(getArchiveId(), au.getArchiveId()) && Objects.equal(getUnitId(), au.getUnitId());
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getArchiveId(), getUnitId());
	}
}
