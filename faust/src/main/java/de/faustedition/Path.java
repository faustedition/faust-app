package de.faustedition;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.restlet.data.Reference;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Path extends ArrayDeque<String> {

	public Path(String path) {
		super(Lists.newArrayList(Iterables.filter(Arrays.asList(path.replaceAll("^/+", "").replaceAll("/+$", "").split("/+")), new Predicate<String>() {
			@Override
			public boolean apply(@Nullable String input) {
				return !input.isEmpty();
			}
		})));
	}

	public Path(Reference reference) {
		this(Objects.firstNonNull(reference.getPath(true), ""));
	}

	public static Path relativeTo(Reference reference) {
		final Reference relativeRef = reference.getRelativeRef();
		return (".".equals(relativeRef.getPath()) ? new Path("") : new Path(relativeRef));
	}
}
