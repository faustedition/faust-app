package de.faustedition.model.document;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.SortedMap;
import java.util.SortedSet;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.faustedition.util.HibernateUtil;

public class DocumentStructureNode
{
	public static final String PATH_SEPARATOR = "\\";

	private long id;
	private DocumentStructureNodeType nodeType;
	private int nodeOrder;
	private String name;
	private DocumentStructureNode parent;
	private String parentPath;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public DocumentStructureNode getParent()
	{
		return parent;
	}

	public void setParent(DocumentStructureNode parent)
	{
		this.parent = parent;
		setParentPath(parent == null ? null : parent.getPath());
	}

	public String getParentPath()
	{
		return parentPath;
	}

	public void setParentPath(String parentPath)
	{
		this.parentPath = parentPath;
	}

	public String getPath()
	{
		return (parentPath == null ? "" : (parentPath + PATH_SEPARATOR)) + name;
	}

	public Deque<String> getPathComponents()
	{
		ArrayDeque<String> pathComponents = new ArrayDeque<String>();
		for (String pc : StringUtils.split(getPath(), PATH_SEPARATOR))
		{
			pathComponents.add(pc);
		}
		return pathComponents;
	}

	public DocumentStructureNodeType getNodeType()
	{
		return nodeType;
	}

	public void setNodeType(DocumentStructureNodeType nodeType)
	{
		this.nodeType = nodeType;
	}

	public int getNodeOrder()
	{
		return nodeOrder;
	}

	public void setNodeOrder(int nodeOrder)
	{
		this.nodeOrder = nodeOrder;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public boolean equals(Object obj)
	{
		if ((obj != null) && (obj instanceof DocumentStructureNode))
		{
			return getPath().equals(((DocumentStructureNode) obj).getPath());
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return getPath().hashCode();
	}

	public static String[] splitPath(Deque<String> pathComponents)
	{

		if (pathComponents.size() > 1)
		{
			pathComponents = new ArrayDeque<String>(pathComponents);
			String name = pathComponents.removeLast();
			return new String[] { StringUtils.join(pathComponents, PATH_SEPARATOR), name };
		}
		else
		{
			return new String[] { null, pathComponents.peekFirst() };
		}
	}

	public SortedSet<DocumentStructureNode> findChildren(Session session)
	{
		return findChildren(session, getPath());
	}

	public void save(Session session)
	{
		session.saveOrUpdate(this);
	}

	@Override
	public String toString()
	{
		return String.format("%s[%s :: (%s)]", getClass().getSimpleName(), getPath(), getNodeType().toString());
	}

	public static boolean existsAny(Session session)
	{
		return DataAccessUtils.intResult(session.createCriteria(DocumentStructureNode.class).setProjection(Projections.rowCount()).list()) > 0;
	}

	public static DocumentStructureNode findByPath(Session session, Deque<String> pathComponents)
	{
		if (pathComponents.isEmpty())
		{
			return null;
		}

		String[] splitPath = splitPath(pathComponents);
		return findByPath(session, splitPath[0], splitPath[1]);
	}

	public static SortedSet<DocumentStructureNode> findChildren(Session session, String parentPath)
	{
		Criteria childrenCriteria = session.createCriteria(DocumentStructureNode.class).add(parentPath == null ? Restrictions.isNull("parentPath") : Restrictions.eq("parentPath", parentPath));

		SortedSet<DocumentStructureNode> childrenSet = Sets.newTreeSet(NODE_ORDER_COMPARATOR);
		childrenSet.addAll(HibernateUtil.list(childrenCriteria, DocumentStructureNode.class));
		return childrenSet;
	}

	public static SortedSet<DocumentStructureNode> findRootChildren(Session session)
	{
		return findChildren(session, null);
	}

	public static SortedMap<Deque<String>, DocumentStructureNode> findParents(Session session, Deque<String> pathComponents)
	{
		SortedMap<Deque<String>, DocumentStructureNode> parents = Maps.newTreeMap(PATH_LENGTH_COMPARATOR);

		pathComponents = new ArrayDeque<String>(pathComponents);
		pathComponents.removeLast();
		if (pathComponents.isEmpty())
		{
			return parents;
		}

		Criteria parentsCriteria = session.createCriteria(DocumentStructureNode.class);
		Disjunction pathDisjunction = Restrictions.disjunction();
		while (!pathComponents.isEmpty())
		{
			String[] splitPath = splitPath(pathComponents);

			Conjunction pathConjunction = Restrictions.conjunction();
			pathConjunction.add(splitPath[0] == null ? Restrictions.isNull("parentPath") : Restrictions.eq("parentPath", splitPath[0]));
			pathConjunction.add(Restrictions.eq("name", splitPath[1]));
			pathDisjunction.add(pathConjunction);

			pathComponents.removeLast();
		}

		parentsCriteria.add(pathDisjunction);
		for (DocumentStructureNode node : HibernateUtil.scroll(parentsCriteria, DocumentStructureNode.class))
		{
			parents.put(node.getPathComponents(), node);
		}
		return parents;
	}

	public static DocumentStructureNode findByPath(Session session, String parentPath, String name)
	{
		return DataAccessUtils.uniqueResult(HibernateUtil.list(session.createCriteria(DocumentStructureNode.class).add(
				parentPath == null ? Restrictions.isNull("parentPath") : Restrictions.eq("parentPath", parentPath)).add(Restrictions.eq("name", name)), DocumentStructureNode.class));
	}

	private static Comparator<Deque<String>> PATH_LENGTH_COMPARATOR = new Comparator<Deque<String>>()
	{
		@Override
		public int compare(Deque<String> o1, Deque<String> o2)
		{
			return o1.size() - o2.size();
		}
	};

	private static Comparator<DocumentStructureNode> NODE_ORDER_COMPARATOR = new Comparator<DocumentStructureNode>()
	{

		@Override
		public int compare(DocumentStructureNode o1, DocumentStructureNode o2)
		{
			return o1.nodeOrder - o2.nodeOrder;
		}

	};
}
