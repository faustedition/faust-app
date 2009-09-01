package de.abohnenkamp.paralipomena;

import org.w3c.dom.Element;

public class ParalipomenonTranscription {
	private String callNumber;
	private Element text;
	private Element commentary;

	public ParalipomenonTranscription(String callNumber, Element text, Element commentary) {
		this.callNumber = callNumber;
		this.text = text;
		this.commentary = commentary;
	}

	public String getCallNumber() {
		return callNumber;
	}

	public Element getText() {
		return text;
	}

	public Element getCommentary() {
		return commentary;
	}

}
