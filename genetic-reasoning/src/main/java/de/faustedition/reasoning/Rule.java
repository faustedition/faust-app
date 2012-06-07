package de.faustedition.reasoning;

/**
 * An implication of the form
 * premise(o,p) => related(o,p)
 *
 */
public interface Rule<E> {
	public boolean premise(E o, E p);
	
	public String getName();
}
