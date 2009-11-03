package de.faustedition.model.metadata;

import java.io.Serializable;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;

import de.faustedition.model.search.SearchIndex;

public class MetadataAssignment implements Serializable
{
	private long id;
	private String associatedType;
	private long associatedId;
	private String field;
	private String value;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getAssociatedType()
	{
		return associatedType;
	}

	public void setAssociatedType(String associatedType)
	{
		this.associatedType = associatedType;
	}

	public long getAssociatedId()
	{
		return associatedId;
	}

	public void setAssociatedId(long associatedId)
	{
		this.associatedId = associatedId;
	}

	public String getField()
	{
		return field;
	}

	public void setField(String field)
	{
		this.field = field;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public MetadataFieldDefinition getDefinition()
	{
		return MetadataFieldDefinition.REGISTRY_LOOKUP_TABLE.get(field);
	}

	public static void purgeAll(Session session, String associatedType, long associatedId)
	{
		session.createQuery("DELETE FROM MetadataAssignment WHERE associatedType = :type AND associatedId = :id").setParameter("type", associatedType).setParameter("id", associatedId)
				.executeUpdate();
	}

	public static MetadataAssignment find(Session session, MetadataAssignment assignment)
	{
		return (MetadataAssignment) DataAccessUtils.uniqueResult(session.createCriteria(MetadataAssignment.class).add(Restrictions.eq("associatedType", assignment.getAssociatedType())).add(
				Restrictions.eq("associatedId", assignment.getAssociatedId())).add(Restrictions.eq("field", assignment.getField())).list());
	}

	public void create(Session session)
	{
		session.save(this);
	}

	@SuppressWarnings("unchecked")
	public static List<MetadataAssignment> find(Session session, String associatedType, long associatedId)
	{
		return session.createCriteria(MetadataAssignment.class).add(Restrictions.eq("associatedType", associatedType)).add(Restrictions.eq("associatedId", associatedId)).list();
	}

	public Document getLuceneDocument()
	{
		Document document = new Document();
		document.add(new Field("class", getClass().getName(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
		document.add(new Field("id", Long.toString(getId()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
		document.add(new Field("associatedType", getAssociatedType(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
		document.add(new Field("associatedId", Long.toString(getAssociatedId()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
		document.add(new Field("field", getField(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
		document.add(new Field("value", getValue(), Field.Store.NO, Field.Index.ANALYZED));
		document.add(new Field(SearchIndex.DEFAULT_FIELD, getValue(), Field.Store.NO, Field.Index.ANALYZED));
		return document;
	}
}
