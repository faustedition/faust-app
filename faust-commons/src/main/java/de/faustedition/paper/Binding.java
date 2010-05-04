package de.faustedition.paper;

import java.util.List;

import de.faustedition.document.DocumentUnit;

public class Binding {
	private DocumentUnit documentUnit;
	private List<Integer> offsets;
	private boolean primary;

	public List<Integer> getOffsets() {
		return offsets;
	}

	public void setOffsets(List<Integer> offsets) {
		this.offsets = offsets;
	}

	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}
}
