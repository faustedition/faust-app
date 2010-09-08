package de.faustedition.metadata;

import org.w3c.dom.Element;


public class BohnenkampParalipomenonTranscription {
	private GSACallNumber callNumber;
	private Element text;
	private Element commentary;

	public BohnenkampParalipomenonTranscription(GSACallNumber callNumber, Element text, Element commentary) {
		this.callNumber = callNumber;
		this.text = text;
		this.commentary = commentary;
	}

	public GSACallNumber getCallNumber() {
		return callNumber;
	}

	public Element getText() {
		return text;
	}

	public Element getCommentary() {
		return commentary;
	}

}
