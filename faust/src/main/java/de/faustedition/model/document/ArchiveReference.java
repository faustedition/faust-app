package de.faustedition.model.document;

import de.faustedition.model.hierarchy.HierarchyNodeFacet;

public class ArchiveReference extends HierarchyNodeFacet
{
	private String repository;
	private String callnumber;
	private String legacyCallnumber;

	public String getRepository()
	{
		return repository;
	}

	public void setRepository(String repository)
	{
		this.repository = repository;
	}

	public String getCallnumber()
	{
		return callnumber;
	}

	public void setCallnumber(String callnumber)
	{
		this.callnumber = callnumber;
	}

	public String getLegacyCallnumber()
	{
		return legacyCallnumber;
	}

	public void setLegacyCallnumber(String legacyCallnumber)
	{
		this.legacyCallnumber = legacyCallnumber;
	}
}
