package de.faustedition;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import de.faustedition.facsimile.InternetImageServer;
import de.faustedition.graph.NodeWrapperCollection;
import de.faustedition.graph.NodeWrapperCollectionTemplateModel;
import de.faustedition.User;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Variant;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Templates extends Configuration {

    private static final List<Variant> VARIANTS = Variant.VariantListBuilder.newInstance()
            .mediaTypes(MediaType.TEXT_HTML_TYPE)
            .languages(Locale.GERMAN, Locale.ENGLISH)
            .add()
            .build();

    public Templates(String contextPath, File templateDirectory) {
        super();
        try {
            setSharedVariable("cp", contextPath);
            setSharedVariable("facsimilieIIPUrl", InternetImageServer.BASE_URI.toString());
            setAutoIncludes(Collections.singletonList("/header.ftl"));
            setDefaultEncoding("UTF-8");
            setOutputEncoding("UTF-8");
            setURLEscapingCharset("UTF-8");
            setStrictSyntaxMode(true);
            setWhitespaceStripping(true);
            setTemplateLoader(new FileTemplateLoader(templateDirectory));
            setObjectWrapper(new CustomObjectWrapper());
        } catch (TemplateModelException e) {
            throw Throwables.propagate(e);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public Response render(String name, Map<String, Object> model, Request request, SecurityContext sc) {
        try {
            final Variant variant = Objects.firstNonNull(request.selectVariant(VARIANTS), VARIANTS.get(0));

            model.put("roles", sc == null ? Collections.<String>emptySet() : ((User) sc.getUserPrincipal()).getRoles());
            model.put("message", ResourceBundle.getBundle("messages", variant.getLanguage()));

            final StringWriter entity = new StringWriter();
            getTemplate(name + ".ftl").process(model, entity);

            return Response.ok().variant(variant).entity(entity.toString()).build();
        } catch (TemplateException e) {
            throw new WebApplicationException(e);
        } catch (IOException e) {
            throw new WebApplicationException(e);
        }
    }

    public Response render(String name, Request request, SecurityContext sc) {
        return render(name, Maps.<String, Object>newHashMap(), request, sc);
    }

    public static class CustomObjectWrapper extends DefaultObjectWrapper {

        @Override
        public TemplateModel wrap(Object obj) throws TemplateModelException {
            if (obj instanceof NodeWrapperCollection) {
                return new NodeWrapperCollectionTemplateModel((NodeWrapperCollection<?>) obj);
            }
            return super.wrap(obj);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Object unwrap(TemplateModel model, Class hint) throws TemplateModelException {
            if (model instanceof NodeWrapperCollectionTemplateModel) {
                return ((NodeWrapperCollectionTemplateModel) model).getCollection();
            }
            return super.unwrap(model, hint);
        }
    }
}
