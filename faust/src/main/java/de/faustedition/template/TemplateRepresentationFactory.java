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
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import de.faustedition.FaustURI;
import freemarker.template.Configuration;
import freemarker.template.Template;

@Component
public class TemplateRepresentationFactory {
	private static final Logger LOG = LoggerFactory.getLogger(TemplateRepresentationFactory.class);

	private static final List<Language> SUPPORTED_LANGUAGES = Collections.unmodifiableList(//
			Lists.newArrayList(new Language("de"), new Language("en")));

	@Autowired
	private Configuration configuration;

	public TemplateRepresentation create(String path, ClientInfo client) throws IOException {
		return create(path, client, new HashMap<String, Object>());
	}

	public TemplateRepresentation create(String path, ClientInfo client, Map<String, Object> model) {
		path = path.replaceAll("^/+" ,"").replaceAll("/+$", "");
		final Language language = client.getPreferredLanguage(SUPPORTED_LANGUAGES);
		final Locale locale = (language == null ? Locale.GERMAN : new Locale(language.getName()));

		Template template;
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Getting template for '{}' (locale: '{}')", path, locale);
			}
			template = configuration.getTemplate(path + ".ftl", locale);
			Preconditions.checkNotNull(template, "Cannot find template for " + path);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}

		model.put("roles", Lists.transform(client.getRoles(), Functions.toStringFunction()));
		
		final ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
		if (LOG.isTraceEnabled()) {
			LOG.trace("Putting message resource bundle '{}' into model (requested locale '{}')", messages.getLocale(), locale);
		}
		model.put("message", messages);

		TemplateRepresentation representation = new TemplateRepresentation(template, model, MediaType.TEXT_HTML);
		representation.setLanguages(Collections.singletonList(language));

		return representation;
	}
}
