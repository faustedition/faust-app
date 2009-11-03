package de.faustedition.model.manuscript;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;

import de.faustedition.model.search.SearchIndex;
import de.faustedition.util.XMLUtil;

public class Transcription implements Serializable
{

	private long id;
	private Facsimile facsimile;
	private Date created = new Date();
	private Date lastModified = new Date();
	private byte[] textData;
	private byte[] revisionData;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Facsimile getFacsimile()
	{
		return facsimile;
	}

	public void setFacsimile(Facsimile facsimile)
	{
		this.facsimile = facsimile;
	}

	public Date getCreated()
	{
		return created;
	}

	public void setCreated(Date created)
	{
		this.created = created;
	}

	public Date getLastModified()
	{
		return lastModified;
	}

	public void setLastModified(Date lastModified)
	{
		this.lastModified = lastModified;
	}

	public void modified()
	{
		setLastModified(new Date());
	}

	public byte[] getTextData()
	{
		return textData;
	}

	public void setTextData(byte[] data)
	{
		this.textData = data;
	}

	public boolean hasText()
	{
		return XMLUtil.hasText(XMLUtil.parse(getTextData()).getDocumentElement());
	}

	public byte[] getRevisionData()
	{
		return revisionData;
	}

	public void setRevisionData(byte[] revisionData)
	{
		this.revisionData = revisionData;
	}

	public Document getLuceneDocument()
	{
		StringBuilder defaultFieldValue = new StringBuilder();
		Document document = new Document();
		document.add(new Field("class", getClass().getName(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
		document.add(new Field("id", Long.toString(getId()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
		if (textData != null)
		{
			String textData = XMLUtil.parse(this.textData).getDocumentElement().getTextContent();
			document.add(new Field("textData", textData, Field.Store.NO, Field.Index.ANALYZED));
			defaultFieldValue.append("\n" + textData);
		}
		if (revisionData != null)
		{
			String revisionData = XMLUtil.parse(this.revisionData).getDocumentElement().getTextContent();
			document.add(new Field("revisionData", revisionData, Field.Store.NO, Field.Index.ANALYZED));
			defaultFieldValue.append("\n" + revisionData);
		}
		document.add(new Field(SearchIndex.DEFAULT_FIELD, defaultFieldValue.toString(), Field.Store.NO, Field.Index.ANALYZED));

		return document;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && (obj instanceof Transcription) && (facsimile != null))
		{
			Transcription other = (Transcription) obj;
			if (other.facsimile != null)
			{
				return (facsimile.getId() == other.facsimile.getId());
			}
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return (facsimile == null ? super.hashCode() : new HashCodeBuilder().append(facsimile.getId()).toHashCode());
	}

	public static Transcription find(Session session, Facsimile facsimile)
	{
		return (Transcription) DataAccessUtils.uniqueResult(session.createCriteria(Transcription.class).createCriteria("facsimile").add(Restrictions.idEq(facsimile.getId())).list());
	}
}
