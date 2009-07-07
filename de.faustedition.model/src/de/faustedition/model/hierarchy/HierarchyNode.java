package de.faustedition.model.hierarchy;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import de.faustedition.model.Model;

public class HierarchyNode extends Model {
	private static final String PATH_SEPARATOR = "/";
	private int treeLeft;
	private int treeRight;
	private int treeLevel;
	private HierarchyNodeType nodeType;
	private String path;
	private String name;

	public HierarchyNode() {
		super();
	}

	public HierarchyNode(String name, HierarchyNodeType nodeType) {
		super();
		setName(name);
		setNodeType(nodeType);
	}

	public HierarchyNode(HierarchyNode node) {
		super(node);
		this.treeLeft = node.treeLeft;
		this.treeRight = node.treeRight;
		this.treeLevel = node.treeLevel;
		this.nodeType = node.nodeType;
		this.path = node.path;
		this.name = node.name;
	}

	public int getTreeLeft() {
		return treeLeft;
	}

	public void setTreeLeft(int treeLeft) {
		this.treeLeft = treeLeft;
	}

	public int getTreeRight() {
		return treeRight;
	}

	public void setTreeRight(int treeRight) {
		this.treeRight = treeRight;
	}

	public int getTreeLevel() {
		return treeLevel;
	}

	public void setTreeLevel(int treeLevel) {
		this.treeLevel = treeLevel;
	}

	public boolean isLeafNode() {
		return (this.treeLeft + 1) == this.treeRight;
	}

	public HierarchyNodeType getNodeType() {
		return nodeType;
	}

	public void setNodeType(HierarchyNodeType nodeType) {
		this.nodeType = nodeType;
	}

	public String getNodeTypeValue() {
		return (this.nodeType == null ? null : this.nodeType.toString());
	}

	public void setNodeTypeValue(String value) {
		this.nodeType = (value == null ? null : HierarchyNodeType.valueOf(value));
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String[] getPathComponents() {
		return StringUtils.split(getPath(), PATH_SEPARATOR);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = escapeName(name);
	}

	public static HierarchyNode createRoot() {
		HierarchyNode root = new HierarchyNode();
		root.setNodeType(HierarchyNodeType.FILE);
		root.setTreeLeft(1);
		root.setTreeRight(2);
		root.setTreeLevel(0);
		root.setPath("");
		root.setName("");
		return root;
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj != null) && getClass().equals(obj.getClass())) {
			HierarchyNode other = (HierarchyNode) obj;
			return new EqualsBuilder().append(this.path, other.path).append(this.name, other.name).isEquals();
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.path).append(this.name).toHashCode();
	}

	public String getFullPath() {
		return StringUtils.strip(StringUtils.join(new String[] { path, name }, PATH_SEPARATOR), PATH_SEPARATOR);
	}

	public static String escapeName(String name) {
		return (name == null ? null : name.replaceAll(Pattern.quote(PATH_SEPARATOR), " "));
	}
}