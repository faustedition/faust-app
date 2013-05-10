package de.faustedition;

import com.google.common.base.Objects;
import org.restlet.data.Reference;

import java.util.ArrayDeque;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Path extends ArrayDeque<String> {

	public Path(String path) {
        super(WebApplication.pathDeque(path));
	}

	public Path(Reference reference) {
		this(Objects.firstNonNull(reference.getPath(true), ""));
	}

	public static Path relativeTo(Reference reference) {
		final Reference relativeRef = reference.getRelativeRef();
		return (".".equals(relativeRef.getPath()) ? new Path("") : new Path(relativeRef));
	}
}
