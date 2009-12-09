package de.faustedition.model.document;

public class LegacyMetadataFacet extends DocumentStructureNodeFacet
{
	private String hands;
	private String recordNumber;
	private String geneticLevel;
	private String remarks;

	public String getHands()
	{
		return hands;
	}

	public void setHands(String hands)
	{
		this.hands = hands;
	}

	public String getRecordNumber()
	{
		return recordNumber;
	}

	public void setRecordNumber(String recordNumber)
	{
		this.recordNumber = recordNumber;
	}

	public String getGeneticLevel()
	{
		return geneticLevel;
	}

	public void setGeneticLevel(String geneticLevel)
	{
		this.geneticLevel = geneticLevel;
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
