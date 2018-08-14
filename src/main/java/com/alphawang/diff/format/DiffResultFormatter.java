package com.alphawang.diff.format;

import com.alphawang.diff.DiffResult;
import com.alphawang.diff.Difference;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;

import static com.alphawang.diff.util.ReflectionUtils.getCollectionItemClass;
import static com.alphawang.diff.util.ReflectionUtils.getMapValueClass;
import static com.alphawang.diff.util.ReflectionUtils.getValue;

public class DiffResultFormatter {

    private static final String EMPTY = "";
    private static final String NEW_LINE = "\n";

    public static String format(DiffResult diffResult, Map<Class, Function> converters) {
        if (diffResult == null || !diffResult.hasDifference()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(formatIdentities(diffResult));
        sb.append(NEW_LINE);
        sb.append(formatDifferences(diffResult, converters));

        return sb.toString();
    }

    public static String formatIdentities(DiffResult diffResult) {
        Map identities = diffResult.getIdentities();
        if (identities == null || identities.size() <= 0) {
            return "no-identities";
        }
        return Joiner.on(", ").withKeyValueSeparator(" = ").join(identities);
    }

    public static String formatDifferences(DiffResult diffResult, Map<Class, Function> toStringFunctions) {
        Map<String, Difference> differenceMap = diffResult.getDifferences();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Difference> entry : differenceMap.entrySet()) {
            sb.append(formatDifference(entry.getValue(), toStringFunctions)).append(NEW_LINE);
        }
        return sb.toString();
    }

    private static String formatDifference(Difference difference, Map<Class, Function> toStringFunctions) {
        if (difference == null) {
            return null;
        }

        return getSummaryMsg(difference) + getDetailMsg(difference, toStringFunctions);
    }

    private static String getSummaryMsg(Difference difference) {
        return new StringBuilder()
            .append("[").append(difference.getPath()).append("] ")
            .append(difference.getType())
            .append(" - ")
            .append(defaultString(difference.getSummary()))
            .toString();
    }

    private static String getDetailMsg(Difference difference, Map<Class, Function> toStringFunctions) {
        return new StringBuilder()
            .append(NEW_LINE)
            .append(" [L] ")
            .append(formatObject(difference.getLeftValue(), toStringFunctions))
            .append(NEW_LINE)
            .append(" [R] ")
            .append(formatObject(difference.getRightValue(), toStringFunctions))
            .toString();
    }

    private static String formatObject(Object object, Map<Class, Function> toStringFunctions) {
        if (object instanceof Map) {
            Map map = (Map) object;
            if (isEmpty(map)) {
                return EMPTY;
            }

            Class valueClass = getMapValueClass(map);
            Function valueFunction = getValue(toStringFunctions, valueClass);
            if (valueFunction != null) {
                map = Maps.transformValues(map, valueFunction);
            }

            return map.toString();
        }

        if (object instanceof Collection) {
            Collection collection = (Collection) object;
            if (isEmpty(collection)) {
                return EMPTY;
            }

            Class itemClass = getCollectionItemClass(collection);
            Function itemFunction = getValue(toStringFunctions, itemClass);
            if (itemFunction != null) {
                return Iterables.transform(collection, itemFunction).toString();
            }

            return collection.toString();
        }

        Function itemFunction = getValue(toStringFunctions, object.getClass());
        if (itemFunction != null) {
            return defaultString(itemFunction.apply(object));
        }

        return defaultString(object);
    }

    private static String defaultString(Object value) {
        return value == null ? "{}" : value.toString();
    }
    
    private static boolean isEmpty(Map map) {
        return map == null || map.size() == 0;
    }

    private static boolean isEmpty(Collection collection) {
        return collection == null || collection.size() == 0;
    }
}
