/*
 * #%L
 * Text Resource Combo Utilities
 * %%
 * Copyright (C) 2012 Gregor Middell
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.faustedition.resource;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A text-based resource to be delivered via HTTP.
 * <p/>
 * Next to a reference to the resource's content supplier, this class models some metadata specific to
 * HTTP-based delivery like a URI via which the resource can be referenced directly or the maximum time it can be
 * cached by browsers or proxy servers.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextResource implements InputSupplier<Reader> {
	/**
	 * CSS media type.
	 */
	public static final String TEXT_CSS = "text/css";

	/**
	 * JavaScript source media type.
	 */
	public static final String APPLICATION_JAVASCRIPT = "application/javascript";

	/**
	 * JSON media type.
	 */
	public static final String APPLICATION_JSON = "application/json";

	/**
	 * Plaintext media type.
	 */
	public static final String TEXT_PLAIN = "text/plain";

	/**
	 * XML media type.
	 */
	public static final String APPLICATION_XML = "application/xml";

	/**
	 * Supplier of the resource's content.
	 */
	public final InputSupplier<? extends InputStream> content;

	/**
	 * URI of the resource, used e.g. for link rewriting.
	 */
	public final URI source;

	/**
	 * The character set with which the resource's content is encoded.
	 */
	public final Charset charset;

	/**
	 * When this resource was last modified (milliseconds since epoch).
	 */
	public final long lastModified;

	/**
	 * Maximum age of this resource in a HTTP cache (specified in seconds).
	 */
	public final long maxAge;

	/**
	 * Constructor.
	 *
	 * @param content      assigned to {@link #content}
	 * @param source       assigned to {@link #source}
	 * @param charset      assigned to {@link #charset}
	 * @param lastModified assigned to {@link #lastModified}
	 * @param maxAge       assigned to {@link #maxAge}
	 */
	public TextResource(InputSupplier<? extends InputStream> content, URI source, Charset charset, long lastModified, long maxAge) {
		this.content = content;
		this.source = source;
		this.charset = charset;
		this.lastModified = lastModified;
		this.maxAge = maxAge;
	}

	/**
	 * Determines the media type of the resource by mapping its filename extension in the {@link #source source URI} path.
	 * <p/>
	 * For unknown filename extensions, it returns <code>text/plain</code> as a default.
	 *
	 * @return the media/MIME type
	 */
	public String getMediaType() {
		return Objects.firstNonNull(MIME_TYPES.get(TO_FILENAME_EXTENSION.apply(source.getPath())), TEXT_PLAIN);
	}

	/**
	 * Creates a reader for this resource's content.
	 * <p/>
	 * If the resource is of media type <code>text/css</code>, the reader is wrapped by a {@link CSSURLRewriteFilterReader filter}
	 * which rewrites <code>url()</code> references to external resources on the fly.
	 *
	 * @return a reader for this resource's content
	 * @throws IOException
	 */
	@Override
	public Reader getInput() throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(content.getInput(), charset));
		return (TEXT_CSS.equals(getMediaType()) ? new CSSURLRewriteFilterReader(reader) : reader);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).addValue(source).toString();
	}

	/**
	 * Filters CSS input from a reader in order to rewrite references to external resources.
	 * <p/>
	 * References to external resources in CSS via <code>url()</code> are relative to the location of the CSS stylesheet,
	 * which often results in them becoming invalid upon {@link TextResourceCombo resource combination}. This filter
	 * uses the CSS stylesheet's {@link TextResource#source source URI} to resolve external references against it on the fly.
	 */
	protected class CSSURLRewriteFilterReader extends Reader {
		private final Reader in;
		private StringReader buf;

		private CSSURLRewriteFilterReader(Reader in) {
			this.in = in;
		}

		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			if (buf == null) {
				final String css = CharStreams.toString(in);
				final Matcher urlRefMatcher = URL_REF_PATTERN.matcher(css);
				final StringBuffer rewritten = new StringBuffer(css.length());
				while (urlRefMatcher.find()) {
					urlRefMatcher.appendReplacement(rewritten, "url(" + source.resolve(urlRefMatcher.group(1)) + ")");
				}
				urlRefMatcher.appendTail(rewritten);
				buf = new StringReader(rewritten.toString());
			}
			return buf.read(cbuf, off, len);
		}

		@Override
		public void close() throws IOException {
			Closeables.close(buf, false);
		}
	}

	private static final Pattern URL_REF_PATTERN = Pattern.compile("url\\(([^\\)]+)\\)");

	private static Map<String, String> MIME_TYPES = Maps.newHashMap();

	static {
		MIME_TYPES.put("css", TEXT_CSS);
		MIME_TYPES.put("js", APPLICATION_JAVASCRIPT);
		MIME_TYPES.put("json", APPLICATION_JSON);
		MIME_TYPES.put("txt", TEXT_PLAIN);
		MIME_TYPES.put("xml", APPLICATION_XML);
	}

	private static final Function<String, String> TO_FILENAME_EXTENSION = new Function<String, String>() {
		@Override
		public String apply(String input) {
			return Objects.firstNonNull(Files.getFileExtension(input), "").toLowerCase();
		}
	};
}
