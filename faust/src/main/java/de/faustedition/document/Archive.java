package de.faustedition.document;

import com.google.common.base.Objects;

public class Archive extends DocumentUnit {
	private String archiveId;
	private String name;

	public String getArchiveId() {
		return archiveId;
	}

	public void setArchiveId(String archiveId) {
		this.archiveId = archiveId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Archive) {
			return Objects.equal(getArchiveId(), ((Archive) obj).getArchiveId());
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return getArchiveId().hashCode();
	}
}
