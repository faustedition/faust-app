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
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.faustedition.facsimile.InternetImageServer;
import de.faustedition.graph.NodeWrapperCollection;
import de.faustedition.graph.NodeWrapperCollectionTemplateModel;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.core.Environment;
import freemarker.ext.beans.ArrayModel;
import freemarker.ext.beans.BooleanModel;
import freemarker.ext.beans.CollectionModel;
import freemarker.ext.beans.MapModel;
import freemarker.ext.beans.NumberModel;
import freemarker.ext.beans.StringModel;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
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

    private static final List<Variant> VARIANTS = Variant.VariantListBuilder.newInstance()
            .mediaTypes(MediaType.TEXT_HTML_TYPE)
            .languages(Locale.GERMAN, Locale.ENGLISH)
            .add()
            .build();

    @Inject
    public Templates(Configuration configuration, ObjectMapper objectMapper) {
        super();
        try {
            final String templateRootPath = configuration.property("faust.template_root");
            setTemplateLoader(templateRootPath.isEmpty()
                    ? new ClassTemplateLoader(getClass(), "/template")
                    : new FileTemplateLoader(new File(templateRootPath))
            );
            setSharedVariable("cp", configuration.property("faust.context_path"));
            setSharedVariable("yp", configuration.property("faust.yui_path"));
            setSharedVariable("facsimileIIPUrl", InternetImageServer.BASE_URI.toString());
            setSharedVariable("debug", Boolean.parseBoolean(configuration.property("faust.debug")));
            setSharedVariable("json", new ObjectMapperDirective(objectMapper));

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
        try {
            final Variant variant = Objects.firstNonNull(request.selectVariant(VARIANTS), VARIANTS.get(0));

            viewAndModel.put("message", ResourceBundle.getBundle("messages", variant.getLanguage()));

            final StringWriter entity = new StringWriter();
            getTemplate(viewAndModel.viewName() + ".ftl", variant.getLanguage()).process(viewAndModel, entity);

            return Response.ok().variant(variant).entity(entity.toString()).build();
        } catch (TemplateException e) {
            throw new WebApplicationException(e);
        } catch (IOException e) {
            throw new WebApplicationException(e);
        }
    }

    public static class ViewAndModel extends HashMap<String, Object> {

        private final String viewName;

        public ViewAndModel(String viewName) {
            this.viewName = viewName;
        }

        public ViewAndModel add(String key, Object value) {
            put(key, value);
            return this;
        }

        public String viewName() {
            return viewName;
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
                    return new BooleanModel(node.asBoolean(), this);
                } else if (node.isNumber()) {
                    return new NumberModel(node.numberValue(), this);
                } else if (node.isTextual()) {
                    return new StringModel(node.asText(), this);
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
            addSerializer(AdapterTemplateModel.class, TEMPLATE_MODEL_SERIALIZER);
        }

        private static final JsonSerializer<AdapterTemplateModel> TEMPLATE_MODEL_SERIALIZER = new StdSerializer<AdapterTemplateModel>(AdapterTemplateModel.class) {

            @Override
            public void serialize(AdapterTemplateModel value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                final Object adaptedObject = value.getAdaptedObject(Object.class);
                provider.findValueSerializer(adaptedObject.getClass(), null).serialize(adaptedObject, jgen, provider);
            }

        };
    }
}
