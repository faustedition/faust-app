package de.faustedition.collation;

import java.util.Iterator;

import com.google.common.collect.Iterators;

public class Alignment {

	private final float score;
	private final Token[] tokens;

	protected Alignment(float score, Token... tokens) {
		this.score = score;
		this.tokens = tokens;

	}

	public float getScore() {
		return score;
	}

	public Iterator<Token> iterator() {
		return Iterators.forArray(tokens);
	}

	public Token getFirst() {
		return (tokens.length < 1 ? null : tokens[0]);
	}

	public Token getSecond() {
		return (tokens.length < 2 ? null : tokens[1]);
	}
}