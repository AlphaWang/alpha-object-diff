package com.alphawang.diff.util;

import com.google.common.collect.ImmutableList;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReflectionUtils {

    private static final List<String> IGNORE_FIELDS = ImmutableList.of("serialVersionUID");

    public static Map<String, Field> getFields(Class clazz) {
        Map<String, Field> fields = new LinkedHashMap<>();

        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!field.isSynthetic() && !IGNORE_FIELDS.contains(field.getName())) {
                    fields.put(field.getName(), field);
                }
            }
            clazz = clazz.getSuperclass();
        }

        return fields;
    }

    public static Class getMapValueClass(Map map) {
        if (map == null || map.size() <= 0) {
            return Object.class;
        }

        return getCollectionItemClass(map.values());
    }

    public static Class getCollectionItemClass(Collection collection) {
        if (collection == null || collection.size() <= 0) {
            return Object.class;
        }

        Object item = collection.iterator().next();
        if (item == null) {
            return Object.class;
        }

        return item.getClass();
    }

    public static <T> T getValue(Map<Class, T> map, Class key) {
        if (map == null || map.size() <= 0) {
            return null;
        }

        while (key != null && key != Object.class) {
            T value = map.get(key);
            if (value != null) {
                return value;
            }
            key = key.getSuperclass();
        }

        return null;
    }

    public static boolean isSimpleClass(Object obj) {
        Class clazz = obj.getClass();
        boolean assignableFromNumber = Number.class.isAssignableFrom(clazz);
        return clazz.isPrimitive()
            || assignableFromNumber
            || String.class == clazz
            || Character.class == clazz
            || Boolean.class == clazz
            || BigDecimal.class == clazz
            || Date.class == clazz
            || LocalDate.class == clazz
            || java.sql.Date.class == clazz
            || Timestamp.class == clazz;
    }
}
