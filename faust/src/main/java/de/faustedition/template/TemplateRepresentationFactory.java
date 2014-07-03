/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.faustedition.template;

import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.restlet.data.ClientInfo;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

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

		final SortedMap<String, String> textTableOfContents = new TreeMap<String, String>();
		/* TODO add toc
		for (Map.Entry<FaustURI, String> tocEntry : textManager.tableOfContents().entrySet()) {
			final String textUriPath = tocEntry.getKey().getPath();
			final String textName = textUriPath.substring("/text/".length(), textUriPath.length() - ".xml".length());
			textTableOfContents.put(textName, tocEntry.getValue());
		}
		model.put("textToc", textTableOfContents);
        */
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
}
