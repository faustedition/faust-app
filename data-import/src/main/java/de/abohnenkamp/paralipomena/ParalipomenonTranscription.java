package de.abohnenkamp.paralipomena;

import org.w3c.dom.Element;

import de.swkk.metadata.GSACallNumber;

public class ParalipomenonTranscription
{
	private GSACallNumber callNumber;
	private Element text;
	private Element commentary;

	public ParalipomenonTranscription(GSACallNumber callNumber, Element text, Element commentary)
	{
		this.callNumber = callNumber;
		this.text = text;
		this.commentary = commentary;
	}

	public GSACallNumber getCallNumber()
	{
		return callNumber;
	}

	public Element getText()
	{
		return text;
	}

	public Element getCommentary()
	{
		return commentary;
	}

}
