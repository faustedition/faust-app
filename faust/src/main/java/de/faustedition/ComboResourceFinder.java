package de.faustedition;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import net.middell.combo.*;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Finder;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Singleton
public class ComboResourceFinder extends Finder {

	private TextResourceResolver resolver;

	@Inject
	public ComboResourceFinder(@Named("static.home") String staticHome, @Named("ctx.path") String contextPath) {
		resolver = new TextResourceResolver();
		FileBasedTextResourceCollection.register(resolver, "yui-3.3.0/", new File(staticHome, "yui3"), contextPath + "/static/yui3", Charset.forName("UTF-8"), 86400);
	}

	@Override
	public ServerResource find(Request request, Response response) {
		return new ComboResource();
	}

	private class ComboResource extends ServerResource {

		@Override
		protected Representation get() throws ResourceException {
			return new ComboRepresentation(resolver.resolve(extractPaths()));
		}
		protected Iterable<String> extractPaths() {
			final List<String> parameters = Arrays.asList(Objects.firstNonNull(getOriginalRef().getQuery(true), "").split("\\&"));
			return Iterables.filter(Iterables.transform(parameters, new Function<String, String>() {

				@Override
				public String apply(String input) {
					final int equalsIndex = input.indexOf('=');
					final String parameterName = (equalsIndex < 0 ? input : input.substring(0, equalsIndex));
					return Strings.emptyToNull(parameterName.trim());
				}
			}), Predicates.notNull());
		}

	}
	private class ComboRepresentation extends WriterRepresentation {

		private final TextResourceCombo combo;

		private ComboRepresentation(TextResourceCombo combo) {
			super(new MediaType(combo.getMediaType()));
			setModificationDate(new Date(combo.lastModified()));
			this.combo = combo;
		}
		@Override
		public void write(Writer writer) throws IOException {
			CharStreams.copy(combo, writer);
		}

	}

	private class ReferenceBasedTextResourceCollection extends TextResourceCollection {
		private final Reference base;

		public ReferenceBasedTextResourceCollection(Reference base, String sourceURI, Charset charset, long maxAge) {
			super(sourceURI, charset, maxAge);
			this.base = base;
		}

		@Override
		public TextResource resolve(String relativePath) throws IOException, IllegalArgumentException {
			final ClientResource resource = new ClientResource(getContext(), new Reference(base, relativePath).getTargetRef());
			return new TextResource(new InputSupplier<InputStream>() {
				@Override
				public InputStream getInput() throws IOException {
					return resource.get().getStream();
				}
			}, source.resolve(relativePath), charset, resource.head().getModificationDate().getTime(), maxAge);
		}
	}
}
