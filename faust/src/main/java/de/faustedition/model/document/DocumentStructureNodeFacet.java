package de.faustedition.model.document;

import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.google.common.collect.Maps;

import de.faustedition.util.HibernateUtil;

public abstract class DocumentStructureNodeFacet
{
	protected long id;
	protected DocumentStructureNode facettedNode;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public DocumentStructureNode getFacettedNode()
	{
		return facettedNode;
	}

	public void setFacettedNode(DocumentStructureNode facettedNode)
	{
		this.facettedNode = facettedNode;
	}

	@Override
	public boolean equals(Object obj)
	{
		if ((obj != null) && (getClass().equals(obj.getClass())))
		{
			return facettedNode.equals(((DocumentStructureNodeFacet) obj).facettedNode);
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return facettedNode.hashCode();
	}

	public void save(Session session)
	{
		session.saveOrUpdate(this);
	}

	public static Map<Class<? extends DocumentStructureNodeFacet>, DocumentStructureNodeFacet> findByNode(Session session, DocumentStructureNode node)
	{
		Map<Class<? extends DocumentStructureNodeFacet>, DocumentStructureNodeFacet> facets = Maps.newHashMap();
		Criteria facetCriteria = session.createCriteria(DocumentStructureNodeFacet.class).createCriteria("facettedNode").add(Restrictions.idEq(node.getId()));
		for (DocumentStructureNodeFacet facet : HibernateUtil.scroll(facetCriteria, DocumentStructureNodeFacet.class))
		{
			facets.put(facet.getClass(), facet);
		}
		return facets;
	}
}
