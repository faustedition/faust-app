package de.faustedition.genesis;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ParalipomenonReference implements Serializable {
	private String name;
	private String portfolio;
	private String manuscript;

	public ParalipomenonReference(String name, String portfolio, String manuscript) {
		this.name = name;
		this.portfolio = portfolio;
		this.manuscript = manuscript;
	}

	public String getName() {
		return name;
	}

	public String getPortfolio() {
		return portfolio;
	}

	public String getManuscript() {
		return manuscript;
	}
}