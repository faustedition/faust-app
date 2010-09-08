package de.faustedition.template;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

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

import freemarker.template.Configuration;
import freemarker.template.Template;

@Singleton
public class TemplateRepresentationFactory {
    private static final List<Language> SUPPORTED_LANGUAGES = Collections.unmodifiableList(//
            Lists.newArrayList(new Language("de"), new Language("en")));

    private final Configuration configuration;

    @Inject
    public TemplateRepresentationFactory(Configuration configuration) {
        this.configuration = configuration;
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
        
        TemplateRepresentation representation = new TemplateRepresentation(template, model, MediaType.TEXT_HTML);
        representation.setLanguages(Collections.singletonList(language));
        
        return representation;
    }

    /**
     * Copied from {@link ConnegUtils#getPreferredMetadata(List, List)} and patched to correctly update <code>maxQuality</code>.
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
