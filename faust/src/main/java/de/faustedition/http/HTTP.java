package de.faustedition.http;

import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.Deque;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class HTTP {

    private static final Joiner PATH_JOINER = Joiner.on('/').skipNulls();
    private static final Splitter PATH_SPLITTER = Splitter.on('/').omitEmptyStrings().trimResults();

    public static Deque<String> pathDeque(String path) {
        return Lists.newLinkedList(PATH_SPLITTER.split(path));
    }

    public static String normalizePath(String path) {
        final StringBuffer pathBuf = new StringBuffer(Strings.nullToEmpty(path));
        for (int len = pathBuf.length(); len > 0 && pathBuf.charAt(len - 1) == '/'; len--) {
            pathBuf.delete(len - 1, len);
        }
        return pathBuf.toString();
    }

    public static String joinPath(Iterable<String> components) {
        return PATH_JOINER.join(Iterables.filter(components, Predicates.not(Predicates.equalTo(""))));
    }

    public static String joinPath(String... components) {
        return joinPath(Arrays.asList(components));
    }
}
