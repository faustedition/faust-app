package de.faustedition.textstream;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import eu.interedition.text.stream.TextAnnotationEnd;
import eu.interedition.text.stream.TextAnnotationStart;
import eu.interedition.text.stream.TextContent;
import eu.interedition.text.stream.TextToken;
import freemarker.core.Environment;
import freemarker.ext.util.WrapperTemplateModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class TextTemplateDirective implements TemplateDirectiveModel {

    private static final Escaper HTML_ESCAPER = HtmlEscapers.htmlEscaper();

    protected abstract Iterable<TextTemplateAnnotationHandler> createAnnotationHandlers();

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        final Collection paramValues = params.values();
        if (paramValues.isEmpty()) {
            return;
        }
        Object tokens = paramValues.iterator().next();
        if (tokens instanceof WrapperTemplateModel) {
            tokens = ((WrapperTemplateModel) tokens).getWrappedObject();
        }
        if (tokens instanceof Iterable) {
            Iterable<TextTemplateAnnotationHandler> handlers = createAnnotationHandlers();
            final Writer out = env.getOut();
            final Map<String, TextAnnotationStart> annotationContext = Maps.newHashMap();
            final Set<String> classes = Sets.newTreeSet();
            boolean span = false;
            for (TextToken token : Iterables.filter((Iterable<?>) tokens, TextToken.class)) {
                if (token instanceof TextContent) {
                    final String content = ((TextContent) token).getContent();
                    if (!content.isEmpty()) {
                        if (!span) {
                            out.write("<span" + (classes.isEmpty() ? "" : " class=\"" + Joiner.on(" ").join(classes) + "\"") + ">");
                            span = true;
                        }
                        out.write(Joiner.on("<br>\n").join(Iterables.transform(
                                Splitter.on("\n").split(content),
                                HTML_ESCAPER.asFunction()
                        )));
                    }
                } else if (token instanceof TextAnnotationStart) {
                    final TextAnnotationStart annotationStart = (TextAnnotationStart) token;
                    annotationContext.put(annotationStart.getId(), annotationStart);
                    boolean classesChanged = false;
                    for (TextTemplateAnnotationHandler handler : handlers) {
                        classesChanged = (handler.start(annotationStart, classes) || classesChanged);
                    }
                    if (classesChanged && span) {
                        out.write("</span>");
                        span = false;
                    }
                } else if (token instanceof TextAnnotationEnd) {
                    final TextAnnotationStart annotationStart = annotationContext.remove(((TextAnnotationEnd) token).getId());
                    boolean classesChanged = false;
                    for (TextTemplateAnnotationHandler handler : handlers) {
                        classesChanged = (handler.end(annotationStart, classes) || classesChanged);
                    }
                    if (classesChanged && span) {
                        out.write("</span>");
                        span = false;
                    }
                }
            }
            if (span) {
                out.write("</span>");
            }
        }
    }

}
