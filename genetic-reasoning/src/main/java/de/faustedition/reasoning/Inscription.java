package de.faustedition.reasoning;

import java.util.Collection;
import java.util.TreeSet;

public class Inscription extends TreeSet<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Inscription() {
		super();
	}
	
	public Inscription(Collection<? extends Integer> c) {
		super(c);
	}
	
	public void addInterval(int from, int to) {
		for (int i = from; i<=to; i++) {
			this.add(i);
		}
	}
}
