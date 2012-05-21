package de.faustedition.genesis;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VerseInterval {
	protected int start;
	protected int end;

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	@Override
	public String toString() {
		return new StringBuilder("[").append(getStart()).append(", ").append(getEnd()).append("]").toString();
	}
}
