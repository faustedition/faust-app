package de.faustedition;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.faustedition.facsimile.InternetImageServer;
import de.faustedition.graph.NodeWrapperCollection;
import de.faustedition.graph.NodeWrapperCollectionTemplateModel;
import de.faustedition.http.LastModified;
import eu.interedition.text.stream.NamespaceMapping;
import de.faustedition.textstream.TextTemplateAnnotationHandler;
import de.faustedition.textstream.TextTemplateDirective;
import de.faustedition.transcript.DramaAnnotationTemplateMarkupHandler;
import de.faustedition.transcript.EditAnnotationTemplateMarkupHandler;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.core.Environment;
import freemarker.ext.beans.CollectionModel;
import freemarker.ext.beans.MapModel;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Templates extends freemarker.template.Configuration {

    private static final Logger LOG = Logger.getLogger(Templates.class.getName());

    private static final List<Variant> VARIANTS = Variant.VariantListBuilder.newInstance()
            .mediaTypes(MediaType.TEXT_HTML_TYPE)
            .languages(Locale.GERMAN, Locale.ENGLISH)
            .add()
            .build();

    private final boolean debug;

    @Inject
    public Templates(Configuration configuration, final NamespaceMapping namespaceMapping, ObjectMapper objectMapper) {
        super();
        try {
            this.debug = Boolean.parseBoolean(configuration.property("faust.debug"));

            final String templateRootPath = configuration.property("faust.template_root");
            setTemplateLoader(templateRootPath.isEmpty()
                    ? new ClassTemplateLoader(getClass(), "/template")
                    : new FileTemplateLoader(new File(templateRootPath))
            );
            setSharedVariable("cp", configuration.property("faust.context_path"));
            setSharedVariable("yp", configuration.property("faust.yui_path"));
            setSharedVariable("facsimileIIPUrl", InternetImageServer.BASE_URI.toString());
            setSharedVariable("debug", debug);
            setSharedVariable("json", new ObjectMapperDirective(objectMapper));
            setSharedVariable("transcriptMarkup", new TextTemplateDirective() {

                @Override
                protected Iterable<TextTemplateAnnotationHandler> createAnnotationHandlers() {
                    return Arrays.asList(
                            new DramaAnnotationTemplateMarkupHandler(namespaceMapping),
                            new EditAnnotationTemplateMarkupHandler(namespaceMapping)
                    );
                }
            });

            setAutoIncludes(Collections.singletonList("/header.ftl"));
            setDefaultEncoding("UTF-8");
            setOutputEncoding("UTF-8");
            setURLEscapingCharset("UTF-8");
            setWhitespaceStripping(true);

            setObjectWrapper(new CustomObjectWrapper());
        } catch (TemplateModelException e) {
            throw Throwables.propagate(e);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public Response render(Request request, ViewAndModel viewAndModel) {
        return render(request, Response.ok(viewAndModel).build());
    }

    public Response render(Request request, Response response) {
        try {
            final Variant variant = Objects.firstNonNull(request.selectVariant(VARIANTS), VARIANTS.get(0));
            final ViewAndModel viewAndModel = (ViewAndModel) response.getEntity();

            Date lastModified = response.getLastModified();
            if (debug && lastModified == null) {
                lastModified = new Date(System.currentTimeMillis());
            }

            Response.ResponseBuilder renderedResponse = null;
            if (lastModified != null) {
                renderedResponse = request.evaluatePreconditions(lastModified);
            }
            if (renderedResponse == null) {
                renderedResponse = Response.ok(render(viewAndModel, variant));
            }
            if (lastModified != null) {
                renderedResponse.lastModified(lastModified);
            }
            return renderedResponse.variant(variant).build();
        } catch (TemplateException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new WebApplicationException(e);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new WebApplicationException(e);
        }
    }

    public String render(ViewAndModel viewAndModel, Variant variant) throws IOException, TemplateException {
        variant = Objects.firstNonNull(variant, VARIANTS.get(0));

        viewAndModel.put("message", ResourceBundle.getBundle("messages", variant.getLanguage()));

        final StringWriter entity = new StringWriter();
        getTemplate(viewAndModel.viewName() + ".ftl", variant.getLanguage()).process(viewAndModel, entity);
        return entity.toString();
    }

    public static class ViewAndModel extends HashMap<String, Object> implements LastModified {

        private final String viewName;
        private Date lastModified;

        public ViewAndModel(String viewName) {
            this.viewName = viewName;
        }

        public ViewAndModel(String viewName, Date lastModified) {
            this.viewName = viewName;
            this.lastModified = lastModified;
        }

        public ViewAndModel add(String key, Object value) {
            put(key, value);
            return this;
        }

        public String viewName() {
            return viewName;
        }

        @Override
        public Date lastModified() {
            return lastModified;
        }
    }

    public static class CustomObjectWrapper extends DefaultObjectWrapper {

        @Override
        public TemplateModel wrap(Object obj) throws TemplateModelException {
            if (obj instanceof JsonNode) {
                final JsonNode node = (JsonNode) obj;
                if (node.isObject()) {
                    final Map<String, JsonNode> map = Maps.newHashMapWithExpectedSize(node.size());
                    for (Iterator<Map.Entry<String,JsonNode>> it = node.fields(); it.hasNext(); ) {
                        Map.Entry<String, JsonNode> field = it.next();
                        map.put(field.getKey(), field.getValue());
                    }
                    return new MapModel(map, this);
                } else if (node.isArray()) {
                    return new CollectionModel(Lists.newArrayList(node), this);
                } else if (node.isBoolean()) {
                    return (node.asBoolean() ? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE);
                } else if (node.isFloatingPointNumber()) {
                    return new SimpleNumber(node.asDouble());
                } else if (node.isIntegralNumber()) {
                    return new SimpleNumber(node.asLong());
                } else if (node.isTextual()) {
                    return new SimpleScalar(node.asText());
                } else if (node.isNull()) {
                    return null;
                } else {
                    throw new TemplateModelException(node.toString());
                }
            }
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

    public static class ObjectMapperDirective implements TemplateDirectiveModel {

        private final ObjectWriter objectWriter;

        public ObjectMapperDirective(ObjectMapper objectMapper) {
            this.objectWriter = objectMapper.writer();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
            objectWriter.writeValue(
                    env.getOut(),
                    (params.size() == 1 ? Iterables.getOnlyElement(params.values()) : params)
            );
        }
    }

    /**
     * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
     */
    public static class TemplateModule extends SimpleModule {

        public TemplateModule() {
            super(Version.unknownVersion());
            addSerializer(SimpleNumber.class, NUMBER_TEMPLATE_MODEL_SERIALIZER);
            addSerializer(SimpleScalar.class, STRING_TEMPLATE_MODEL_SERIALIZER);
            addSerializer(TemplateBooleanModel.class, BOOLEAN_TEMPLATE_MODEL_SERIALIZER);
            addSerializer(AdapterTemplateModel.class, ADAPTER_TEMPLATE_MODEL_SERIALIZER);
        }

        private static final JsonSerializer<SimpleNumber> NUMBER_TEMPLATE_MODEL_SERIALIZER = new StdSerializer<SimpleNumber>(SimpleNumber.class) {
            @Override
            public void serialize(SimpleNumber value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                final Number number = value.getAsNumber();
                if (number instanceof Double) {
                    jgen.writeNumber((Double) number);
                } else if (number instanceof Long) {
                    jgen.writeNumber((Long) number);
                } else {
                    throw new IllegalArgumentException(number.toString());
                }
            }
        };

        private static final JsonSerializer<AdapterTemplateModel> ADAPTER_TEMPLATE_MODEL_SERIALIZER = new StdSerializer<AdapterTemplateModel>(AdapterTemplateModel.class) {

            @Override
            public void serialize(AdapterTemplateModel value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                final Object adaptedObject = value.getAdaptedObject(Object.class);
                provider.findValueSerializer(adaptedObject.getClass(), null).serialize(adaptedObject, jgen, provider);
            }
        };

        private static final JsonSerializer<SimpleScalar> STRING_TEMPLATE_MODEL_SERIALIZER = new StdSerializer<SimpleScalar>(SimpleScalar.class) {
            @Override
            public void serialize(SimpleScalar value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                jgen.writeString(value.getAsString());
            }
        };

        private static final JsonSerializer<TemplateBooleanModel> BOOLEAN_TEMPLATE_MODEL_SERIALIZER = new StdSerializer<TemplateBooleanModel>(TemplateBooleanModel.class) {
            @Override
            public void serialize(TemplateBooleanModel value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                try {
                    jgen.writeBoolean(value.getAsBoolean());
                } catch (TemplateModelException e) {
                    throw Throwables.propagate(e);
                }
            }
        };
    }
}
