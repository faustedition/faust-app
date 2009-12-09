package de.faustedition.model.document;

import org.hibernate.Session;

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
}
