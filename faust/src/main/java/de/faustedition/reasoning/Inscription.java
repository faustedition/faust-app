package de.faustedition.reasoning;

import java.util.Collection;
import java.util.TreeSet;

public class Inscription extends TreeSet<Integer> {

	private static final long serialVersionUID = 1L;

	private final String name;

	public Inscription(String name) {
		super();
		this.name = name;

	}

	public Inscription(Collection<? extends Integer> c, String name) {
		super(c);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addInterval(int from, int to) {
		for (int i = from; i <= to; i++) {
			this.add(i);
		}
	}

	public boolean spans(Inscription other) {
		return (first() < other.first()) && (last() > other.last());
	}

	@Override
	public String toString() {
		return name;
	}
}
