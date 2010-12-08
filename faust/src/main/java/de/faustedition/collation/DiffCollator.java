package de.faustedition.collation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterables;

public class DiffCollator {
	public List<Alignment> align(Iterable<Token> a, Iterable<Token> b) {
		final Token[] at = Iterables.toArray(a, Token.class);
		final Token[] bt = Iterables.toArray(b, Token.class);
		int ai = 0;
		int bi = 0;

		final List<Alignment> alignments = new ArrayList<Alignment>(Math.max(at.length, bt.length));
		final Iterator<Difference> diffIterator = new Diff<Token>(at, bt, DEFAULT_MATCHER).diff().iterator();

		Difference diff = (diffIterator.hasNext() ? diffIterator.next() : null);
		do {
			if (diff == null) {
				alignments.add(new Alignment(0, at[ai++], bt[bi++]));
				continue;
			}

			if (ai < diff.getDeletedStart() && bi < diff.getAddedStart()) {
				alignments.add(new Alignment(0, at[ai++], bt[bi++]));
				continue;
			}

			if (ai <= diff.getDeletedEnd() && bi <= diff.getAddedEnd()) {
				alignments.add(new Alignment(1, at[ai++], bt[bi++]));
				continue;
			}

			if (ai <= diff.getDeletedEnd() && bi >= diff.getAddedEnd()) {
				alignments.add(new Alignment(1, at[ai++], null));
				continue;
			}

			if (bi <= diff.getAddedEnd() && ai >= diff.getDeletedEnd()) {
				alignments.add(new Alignment(1, null, bt[bi++]));
				continue;
			}

			diff = (diffIterator.hasNext() ? diffIterator.next() : null);
		} while (ai < at.length || bi < bt.length);

		return alignments;
	}
	
	private static Comparator<Token> DEFAULT_MATCHER = new Comparator<Token>() {

		@Override
		public int compare(Token o1, Token o2) {
			return o1.text().trim().compareToIgnoreCase(o2.text().trim());
		}
	};
}
