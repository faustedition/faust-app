package de.faustedition.model.hierarchy;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.SortedSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.support.DataAccessUtils;

import com.google.common.collect.Sets;

import de.faustedition.util.HibernateUtil;

public class HierarchyNode {
	public static final String PATH_SEPARATOR = "/";

	private long id;
	private HierarchyNodeType nodeType;
	private String name;
	private HierarchyNode parent;
	private String parentPath;

	public HierarchyNode() {
	}

	public HierarchyNode(HierarchyNode parent, String name, HierarchyNodeType nodeType) {
		setParent(parent);
		setName(name);
		setNodeType(nodeType);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public HierarchyNode getParent() {
		return parent;
	}

	public void setParent(HierarchyNode parent) {
		this.parent = parent;
		setParentPath(parent == null ? "" : parent.getPath());
	}

	public boolean isRoot() {
		return (getParent() == null);
	}

	public String getParentPath() {
		return parentPath;
	}

	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}

	public String getPath() {
		return getPath(parentPath, name);
	}

	public static String getPath(String parentPath, String name) {
		return ("".equals(parentPath) ? "" : (parentPath + PATH_SEPARATOR)) + name;
	}

	public static String[] splitPath(Deque<String> pathComponents) {
		if (pathComponents.isEmpty()) {
			return new String[] { "", "" };
		} else if (pathComponents.size() == 1) {
			return new String[] { "", pathComponents.peekFirst() };
		} else {
			pathComponents = new ArrayDeque<String>(pathComponents);
			String name = pathComponents.removeLast();
			return new String[] { StringUtils.join(pathComponents, PATH_SEPARATOR), name };
		}
	}

	public Deque<String> getPathComponents() {
		ArrayDeque<String> pathComponents = new ArrayDeque<String>();
		for (String pc : StringUtils.split(getPath(), PATH_SEPARATOR)) {
			pathComponents.add(pc);
		}
		return pathComponents;
	}

	public HierarchyNodeType getNodeType() {
		return nodeType;
	}

	public void setNodeType(HierarchyNodeType nodeType) {
		this.nodeType = nodeType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj != null) && (obj instanceof HierarchyNode)) {
			return getPath().equals(((HierarchyNode) obj).getPath());
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return getPath().hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("path", getPath()).append("type", nodeType).toString();
	}

	public static HierarchyNode rootNode(Session session) {
		try {
			Criteria rootCriteria = session.createCriteria(HierarchyNode.class).add(Restrictions.isNull("parent"));
			return DataAccessUtils.requiredUniqueResult(HibernateUtil.list(rootCriteria, HierarchyNode.class));
		} catch (EmptyResultDataAccessException e) {
			HierarchyNode root = new HierarchyNode();
			root.setParent(null);
			root.setName("");
			root.setNodeType(HierarchyNodeType.FILE);
			root.save(session);
			return root;
		}
	}

	public HierarchyNode save(Session session) {
		session.saveOrUpdate(this);
		return this;
	}

	public static boolean existsAny(Session session) {
		Criteria existCriteria = session.createCriteria(HierarchyNode.class).setProjection(Projections.rowCount());
		return DataAccessUtils.intResult(existCriteria.list()) > 0;
	}

	public List<HierarchyNode> findChildren(Session session) {
		Criteria childCriteria = session.createCriteria(HierarchyNode.class);
		childCriteria.createCriteria("parent").add(Restrictions.idEq(getId()));
		return HibernateUtil.list(childCriteria.addOrder(Order.asc("name")), HierarchyNode.class);
	}

	public SortedSet<HierarchyNode> findParents(Session session) {
		SortedSet<HierarchyNode> parents = Sets.newTreeSet(PATH_LENGTH_COMPARATOR);
		if (isRoot()) {
			return parents;
		}

		Criteria parentsCriteria = session.createCriteria(HierarchyNode.class);
		Disjunction pathDisjunction = Restrictions.disjunction();
		Deque<String> pathComponents = getPathComponents();
		do {
			pathComponents.removeLast();
			String[] splitPath = splitPath(pathComponents);

			Conjunction pathConjunction = Restrictions.conjunction();
			pathConjunction.add(Restrictions.eq("parentPath", splitPath[0]));
			pathConjunction.add(Restrictions.eq("name", splitPath[1]));
			pathDisjunction.add(pathConjunction);
		} while (!pathComponents.isEmpty());

		parentsCriteria.add(pathDisjunction);
		parents.addAll(HibernateUtil.list(parentsCriteria, HierarchyNode.class));
		return parents;
	}

	public static HierarchyNode findByPath(Session session, Deque<String> pathComponents) {
		String[] splitPath = splitPath(pathComponents);
		return findByPath(session, splitPath[0], splitPath[1]);
	}

	public static HierarchyNode findByPath(Session session, String parentPath, String name) {
		Criteria pathCriteria = session.createCriteria(HierarchyNode.class);
		pathCriteria.add(Restrictions.eq("parentPath", parentPath));
		pathCriteria.add(Restrictions.eq("name", name));
		return DataAccessUtils.uniqueResult(HibernateUtil.list(pathCriteria, HierarchyNode.class));
	}

	public void addDescendantCriteria(Criteria criteria) {
		if (parent == null) {
			return;
		}
		Disjunction disjunction = Restrictions.disjunction();

		Conjunction self = Restrictions.conjunction();
		self.add(Restrictions.eq("parentPath", parentPath));
		self.add(Restrictions.eq("name", name));
		disjunction.add(self);

		String path = getPath(parentPath, name);
		disjunction.add(Restrictions.eq("parentPath", path));
		disjunction.add(Restrictions.like("parentPath", HibernateUtil.escape(path), MatchMode.START));

		criteria.add(disjunction);
	}

	private static Comparator<HierarchyNode> PATH_LENGTH_COMPARATOR = new Comparator<HierarchyNode>() {

		@Override
		public int compare(HierarchyNode o1, HierarchyNode o2) {
			return (o1.getPath().length() - o2.getPath().length());
		}
	};
}
