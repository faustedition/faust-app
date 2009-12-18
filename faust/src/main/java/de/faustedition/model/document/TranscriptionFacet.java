package de.faustedition.model.document;

import java.util.Date;

import de.faustedition.model.tei.TEIDocument;
import de.faustedition.util.XMLUtil;

public class TranscriptionFacet extends DocumentStructureNodeFacet
{
	private byte[] documentData;
	private Date created = new Date();
	private Date lastModified = new Date();

	public byte[] getDocumentData()
	{
		return documentData;
	}

	public void setDocumentData(byte[] documentData)
	{
		this.documentData = documentData;
	}

	public TEIDocument toTEIDocument()
	{
		return (documentData == null ? null : new TEIDocument(XMLUtil.parse(documentData)));
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
}
