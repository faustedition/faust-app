package de.faustedition.model;

public class Facsimile extends Model {
	private HierarchyNode node;
	private String filePath;

	public HierarchyNode getNode() {
		return node;
	}

	public void setNode(HierarchyNode node) {
		this.node = node;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
}
