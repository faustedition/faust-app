package de.faustedition.template;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;

import org.restlet.data.ClientInfo;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Preference;
import org.restlet.engine.util.ConnegUtils;
import org.restlet.ext.freemarker.TemplateRepresentation;

import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.faustedition.FaustURI;
import de.faustedition.text.TextManager;
import freemarker.template.Configuration;
import freemarker.template.Template;

@Singleton
public class TemplateRepresentationFactory {
	private static final List<Language> SUPPORTED_LANGUAGES = Collections.unmodifiableList(//
			Lists.newArrayList(new Language("de"), new Language("en")));

	private final Configuration configuration;
	private final TextManager textManager;

	@Inject
	public TemplateRepresentationFactory(Configuration configuration, TextManager textManager) {
		this.configuration = configuration;
		this.textManager = textManager;
	}

	public TemplateRepresentation create(String path, ClientInfo client) throws IOException {
		return create(path, client, new HashMap<String, Object>());
	}

	public TemplateRepresentation create(String path, ClientInfo client, Map<String, Object> model) throws IOException {
		final Language language = getPreferredMetadata(SUPPORTED_LANGUAGES, client.getAcceptedLanguages());
		final Locale locale = (language == null ? Locale.GERMAN : new Locale(language.getName()));

		Template template = configuration.getTemplate(path + ".ftl", locale);
		Preconditions.checkNotNull(template, "Cannot find template for " + path);

		model.put("roles", Lists.transform(client.getRoles(), Functions.toStringFunction()));
		model.put("message", ResourceBundle.getBundle("messages", locale));

		final SortedMap<String, String> textTableOfContents = new TreeMap<String, String>();
		for (Map.Entry<FaustURI, String> tocEntry : textManager.tableOfContents().entrySet()) {
			final String textUriPath = tocEntry.getKey().getPath();
			final String textName = textUriPath.substring("/text/".length(), textUriPath.length() - ".xml".length());
			textTableOfContents.put(textName, tocEntry.getValue());
		}
		model.put("textToc", textTableOfContents);

		TemplateRepresentation representation = new TemplateRepresentation(template, model, MediaType.TEXT_HTML);
		representation.setLanguages(Collections.singletonList(language));

		return representation;
	}

	public static class TextTableOfContentsEntry {
		private final String path;
		private final String title;

		public TextTableOfContentsEntry(String path, String title) {
			this.path = path;
			this.title = title;
		}

		public String getPath() {
			return path;
		}

		public String getTitle() {
			return title;
		}
	}

	/**
	 * Copied from {@link ConnegUtils#getPreferredMetadata(List, List)} and
	 * patched to correctly update <code>maxQuality</code>.
	 * 
	 * @see ConnegUtils#getPreferredMetadata(List, List)
	 */
	private static <T extends Metadata> T getPreferredMetadata(List<T> supported, List<Preference<T>> preferences) {
		T result = null;
		float maxQuality = 0;

		if (supported != null) {
			for (Preference<T> pref : preferences) {
				if (supported.contains(pref.getMetadata()) && (pref.getQuality() > maxQuality)) {
					result = pref.getMetadata();
					// was not updated in original method
					maxQuality = pref.getQuality();
				}
			}
		}

		return result;
	}
}
