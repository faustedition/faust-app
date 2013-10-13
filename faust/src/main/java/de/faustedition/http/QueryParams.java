package de.faustedition.http;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import javax.ws.rs.core.MultivaluedMap;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class QueryParams {

    public static final Splitter STRING_LIST_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

    public static String stringVal(MultivaluedMap<String, String> parameters, String name) {
        return stringVal(parameters, name, null);
    }

    public static String stringVal(MultivaluedMap<String, String> parameters, String name, String defaultValue) {
        final String value = Strings.nullToEmpty(parameters.getFirst(name)).trim();
        return (value.isEmpty() ? defaultValue : value);
    }

    public static int intVal(MultivaluedMap<String, String> parameters, String name) {
        return intVal(parameters, name, 0);
    }

    public static int intVal(MultivaluedMap<String, String> parameters, String name, int defaultValue) {
        final String value = stringVal(parameters, name);
        return (value == null ? defaultValue : Integer.parseInt(value));
    }

    public static long longVal(MultivaluedMap<String, String> parameters, String name) {
        return longVal(parameters, name, 0L);
    }

    public static long longVal(MultivaluedMap<String, String> parameters, String name, long defaultValue) {
        final String value = stringVal(parameters, name);
        return (value == null ? defaultValue : Long.parseLong(value));
    }

    public static BigDecimal decimalVal(MultivaluedMap<String, String> parameters, String name) {
        return decimalVal(parameters, name, null);
    }

    public static BigDecimal decimalVal(MultivaluedMap<String, String> parameters, String name, BigDecimal defaultValue) {
        final String value = stringVal(parameters, name);
        return (value == null ? defaultValue : new BigDecimal(value));
    }

    public static boolean boolVal(MultivaluedMap<String, String> parameters, String name) {
        return boolVal(parameters, name, false);
    }

    public static boolean boolVal(MultivaluedMap<String, String> parameters, String name, boolean defaultValue) {
        final String value = stringVal(parameters, name);
        return (value == null ? defaultValue : Boolean.parseBoolean(value));
    }

    public static List<String> stringListVal(MultivaluedMap<String, String> parameters, String name) {
        return stringListVal(parameters, name, null);
    }

    public static List<String> stringListVal(MultivaluedMap<String, String> parameters, String name, List<String> defaultValue) {
        final List<String> value = Lists.newLinkedList(STRING_LIST_SPLITTER.split(stringVal(parameters, name, "")));
        return (value.isEmpty() ? defaultValue : value);
    }
}
