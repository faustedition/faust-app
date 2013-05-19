package de.faustedition;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;

import javax.ws.rs.WebApplicationException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class WebApplication {

    private static final Splitter PATH_SPLITTER = Splitter.on('/').omitEmptyStrings();
    private  static final Joiner PATH_JOINER = Joiner.on("/");


    public static RuntimeException propagateExceptions(Throwable t) throws WebApplicationException {
        final Throwable rootCause = Throwables.getRootCause(t);
        Throwables.propagateIfInstanceOf(rootCause, WebApplicationException.class);
        return Throwables.propagate(t);
    }

    public static Deque<String> pathDeque(String path) {
        final ArrayDeque<String> pathDeque = new ArrayDeque<String>();
        Iterables.addAll(pathDeque, PATH_SPLITTER.split(path));
        return pathDeque;
    }

    public static String path(Deque<String> pathDeque) {
        return PATH_JOINER.join(pathDeque);
    }
}
