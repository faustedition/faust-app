package de.faustedition.resource;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

/**
 * A collection of {@link TextResource text-based resources} which can be {@link TextResourceResolver resolved} via
 * paths with a common prefix.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 * @see #resolve(String)
 */
public abstract class TextResourceCollection {
	/**
	 * The base URI of all resources in this collection via which they can be directly retrieved. This
	 * is used e.g. in the rewriting of links to external resources.
	 */
	protected final Charset charset;

	/**
	 * The character set with with text-based resources in this collection are encoded.
	 */
	protected final URI source;

	/**
	 * The maximum time a resource of this collection may be cached by browsers/HTTP proxies.
	 */
	protected final long maxAge;

	/**
	 * Constructor.
	 * <p/>
	 * To be overridden and augmented by concrete implementations.
	 *
	 * @param sourceURI assigned to {@link #source}
	 * @param charset   assigned to {@link #charset}
	 * @param maxAge    assigned to {@link #maxAge}
	 */
	protected TextResourceCollection(String sourceURI, Charset charset, long maxAge) {
		this.source = URI.create(sourceURI);
		this.charset = charset;
		this.maxAge = maxAge;
	}

	/**
	 * Resolves a path that is relative to the common base path of this collection.
	 *
	 * @param relativePath the relative path
	 * @return the corresponding text-based resource
	 * @throws IllegalArgumentException if the path cannot be resolved
	 * @throws IOException
	 * @see TextResourceResolver#mount(String, TextResourceCollection)
	 */
	public abstract TextResource resolve(String relativePath) throws IOException, IllegalArgumentException;

}
