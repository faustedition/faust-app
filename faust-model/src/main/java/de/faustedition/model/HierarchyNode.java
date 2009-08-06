package de.faustedition.model;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;

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

	@SuppressWarnings("unchecked")
	public static HierarchyNode findRoot(Session session) {
		Criteria rootCriteria = session.createCriteria(HierarchyNode.class).add(Restrictions.eq("treeLevel", 0));
		return (HierarchyNode) DataAccessUtils.requiredUniqueResult(rootCriteria.list());
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

	public boolean hasChild(Session session, String name) {
		Criteria childCriteria = createChildrenCriteria(session).add(Restrictions.eq("name", escapeName(name)));
		return DataAccessUtils.intResult(childCriteria.setProjection(Projections.rowCount()).list()) > 0;
	}

	private Criteria createChildrenCriteria(Session session) {
		Criteria childCriteria = session.createCriteria(getClass());
		childCriteria = childCriteria.add(Restrictions.gt("treeLeft", getTreeLeft()));
		childCriteria = childCriteria.add(Restrictions.lt("treeRight", getTreeRight()));
		childCriteria = childCriteria.add(Restrictions.eq("treeLevel", getTreeLevel() + 1));
		return childCriteria;
	}

	public HierarchyNode add(Session session, HierarchyNode child) {
		child.setPath(getFullPath());
		child.setTreeLeft(getTreeRight());
		child.setTreeRight(getTreeRight() + 1);
		child.setTreeLevel(getTreeLevel() + 1);

		Query updateLeftQuery = session.createQuery("UPDATE HierarchyNode SET treeLeft = treeLeft + 2 WHERE treeLeft > :treeLeft");
		updateLeftQuery.setParameter("treeLeft", getTreeLeft()).executeUpdate();
		Query updateRightQuery = session.createQuery("UPDATE HierarchyNode SET treeRight = treeRight + 2 WHERE treeRight >= :treeRight");
		updateRightQuery.setParameter("treeRight", getTreeRight()).executeUpdate();
		session.clear();

		child.save(session);
		return child;
	}

	public static void clear(Session session) {
		session.createQuery("DELETE HierarchyNode").executeUpdate();
	}

	public HierarchyNode findChild(Session session, String name) {
		return (HierarchyNode) createChildrenCriteria(session).add(Restrictions.eq("name", escapeName(name))).uniqueResult();
	}

	private static String escapeName(String name) {
		return (name == null ? null : name.replaceAll(Pattern.quote(PATH_SEPARATOR), " "));
	}

	@SuppressWarnings("unchecked")
	public static HierarchyNode loadByPath(Session session, String fullPath) {
		String path = (fullPath.contains("/") ? StringUtils.substringBeforeLast(fullPath, "/") : "");
		String name = (fullPath.contains("/") ? StringUtils.substringAfterLast(fullPath, "/") : fullPath);

		Criteria pathCriteria = session.createCriteria(HierarchyNode.class).add(Restrictions.eq("path", path)).add(Restrictions.eq("name", name));
		return (HierarchyNode) DataAccessUtils.requiredUniqueResult(pathCriteria.list());
	}

	@SuppressWarnings("unchecked")
	public List<HierarchyNode> findChildren(Session session) {
		return createChildrenCriteria(session).addOrder(Order.asc("name")).list();
	}
}
