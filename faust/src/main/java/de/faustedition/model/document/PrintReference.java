package de.faustedition.model.document;

import de.faustedition.model.hierarchy.HierarchyNodeFacet;


public class PrintReference extends HierarchyNodeFacet
{
	private String referenceWeimarerAusgabe;
	private String manuscriptReferenceWeimarerAusgabe;
	private String paralipomenonReferenceWeimarerAusgabe;

	public String getReferenceWeimarerAusgabe()
	{
		return referenceWeimarerAusgabe;
	}

	public void setReferenceWeimarerAusgabe(String referenceWeimarerAusgabe)
	{
		this.referenceWeimarerAusgabe = referenceWeimarerAusgabe;
	}

	public String getManuscriptReferenceWeimarerAusgabe()
	{
		return manuscriptReferenceWeimarerAusgabe;
	}

	public void setManuscriptReferenceWeimarerAusgabe(String manuscriptReferenceWeimarerAusgabe)
	{
		this.manuscriptReferenceWeimarerAusgabe = manuscriptReferenceWeimarerAusgabe;
	}

	public String getParalipomenonReferenceWeimarerAusgabe()
	{
		return paralipomenonReferenceWeimarerAusgabe;
	}

	public void setParalipomenonReferenceWeimarerAusgabe(String paralipomenonReferenceWeimarerAusgabe)
	{
		this.paralipomenonReferenceWeimarerAusgabe = paralipomenonReferenceWeimarerAusgabe;
	}
}
