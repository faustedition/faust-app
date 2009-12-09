package de.faustedition.model.document;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class FacsimileFacet extends DocumentStructureNodeFacet
{
	private FacsimileFile facsimileFile;
	private boolean primary = true;

	public FacsimileFile getFacsimileFile()
	{
		return facsimileFile;
	}

	public void setFacsimileFile(FacsimileFile facsimileFile)
	{
		this.facsimileFile = facsimileFile;
	}

	public boolean isPrimary()
	{
		return primary;
	}

	public void setPrimary(boolean primary)
	{
		this.primary = primary;
	}

	@Override
	public boolean equals(Object obj)
	{
		if ((obj != null) && (obj instanceof FacsimileFacet))
		{
			FacsimileFacet other = (FacsimileFacet) obj;
			return facettedNode.equals(other.facettedNode) && facsimileFile.equals(other.facsimileFile);
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder().append(facettedNode).append(facsimileFile).toHashCode();
	}
}
