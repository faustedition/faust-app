package de.faustedition.text;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Functions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractIdleService;
import de.faustedition.textstream.NamespaceMapping;
import de.faustedition.xml.Sources;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Singleton
public class Texts extends AbstractIdleService {

    private static final Pattern TEXT_SOURCE_FILENAME = Pattern.compile("([0-9\\-]+)\\.xml");

    private final Sources sources;
    private final ObjectMapper objectMapper;
    private final NamespaceMapping namespaceMapping;

    private final Map<String, Text> texts = Maps.newTreeMap();
    private String[] keys;

    @Inject
    public Texts(Sources sources, ObjectMapper objectMapper, NamespaceMapping namespaceMapping) {
        this.sources = sources;
        this.objectMapper = objectMapper;
        this.namespaceMapping = namespaceMapping;
    }

    public Map<String, Text> get() {
        return Collections.unmodifiableMap(texts);
    }

    public String[] keys() {
        return keys;
    }

    public static String toKey(Iterable<String> path) throws NumberFormatException {
        final StringBuilder key = new StringBuilder();
        for (String part : path) {
            key.append(String.format("%02d", Integer.parseInt(part)));
        }
        return key.toString();
    }

    public static String[] toPath(String key) {
        final int[] numberedPath = toNumberedPath(key);
        final String[] path = new String[numberedPath.length];
        for (int pc = 0, pl = path.length; pc < pl; pc++) {
            path[pc] = Integer.toString(numberedPath[pc]);
        }
        return path;
    }

    public static int[] toNumberedPath(String key) {
        final int[] path = new int[key.length() / 2];
        for (int kc = 0, kl = key.length(); kc < kl; kc += 2) {
            path[kc / 2] = Integer.parseInt(key.substring(kc, kc + 2));
        }
        return path;
    }

    @Override
    protected void startUp() throws Exception {
        for (File source : sources.directory("text")) {
            final Matcher filenameMatcher = TEXT_SOURCE_FILENAME.matcher(source.getName());
            if (!filenameMatcher.matches()) {
                continue;
            }
            final String key = toKey(Splitter.on("-").split(filenameMatcher.group(1)));
            if (key.startsWith("00")) {
                // FIXME: skip "Urfaust" for now
                continue;
            }
            texts.put(key, new Text(Collections.singletonList(source), 0, objectMapper, namespaceMapping));
        }
        keys = texts.keySet().toArray(new String[texts.size()]);
    }

    @Override
    protected void shutDown() throws Exception {
    }

}
