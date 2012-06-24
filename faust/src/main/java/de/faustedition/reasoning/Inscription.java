package de.faustedition.reasoning;

import java.util.Collection;
import java.util.TreeSet;

public class Inscription extends TreeSet<Integer> {

	private String name;
	
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Inscription(String name) {
		super();
		this.name = name;
		
	}
	
	public Inscription(Collection<? extends Integer> c, String name) {
		super(c);
		this.name = name;
	}
	
	public void addInterval(int from, int to) {
		for (int i = from; i<=to; i++) {
			this.add(i);
		}
	}
	
	@Override
	public String toString() {
		return name;
	}
}
