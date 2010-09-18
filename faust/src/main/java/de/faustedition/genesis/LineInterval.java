package de.faustedition.genesis;

public class LineInterval {
	public int start;
	public int end;
	public String portfolio;
	public String manuscript;

	public LineInterval(String portfolio, String manuscript, int start, int end) {
		this.portfolio = portfolio;
		this.manuscript = manuscript;
		this.start = start;
		this.end = end;
	}
}