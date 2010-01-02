package de.faustedition.model.document;

import java.util.Date;

import de.faustedition.model.hierarchy.HierarchyNodeFacet;

public class DocumentDating extends HierarchyNodeFacet
{
	private Date fromDate;
	private Date toDate;
	private String remarks;

	public Date getFromDate()
	{
		return fromDate;
	}

	public void setFromDate(Date fromDate)
	{
		this.fromDate = fromDate;
	}

	public Date getToDate()
	{
		return toDate;
	}

	public void setToDate(Date toDate)
	{
		this.toDate = toDate;
	}

	public String getRemarks()
	{
		return remarks;
	}

	public void setRemarks(String remarks)
	{
		this.remarks = remarks;
	}

}
