package io.soffa.foundation.commons;


import io.soffa.foundation.core.exceptions.TechnicalException;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class ClassUtil {

    public static Class<?> getClassFromGenericInterface(Class<?> type, Type interfaceType) {
        return getClassFromGenericInterface(type, interfaceType, 0);
    }

    public static List<Field> getDeclaredFields(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields()).filter(field -> {
            // EL
            return !Modifier.isTransient(field.getModifiers());
        }).collect(Collectors.toList());
    }

    /*public static List<Type> lookupGenericInterfaces(Class<?> type) {
        List<Type> interfaces = new ArrayList<>(Arrays.asList(type.getGenericInterfaces()));
        if (type.getSuperclass() != null) {
            interfaces.addAll(lookupGenericInterfaces(type.getSuperclass()));
        }
        return interfaces;
    }*/

    public static Class<?> getClassFromGenericInterface(Class<?> type, Type interfaceType, int argumentIndex) {

        List<Type> parents = new ArrayList<>(Arrays.asList(type.getGenericInterfaces()));
        parents.addAll(Collections.singletonList(type.getGenericSuperclass()));

        for (Type gi : parents) {
            if (!(gi instanceof ParameterizedType)) {
                continue;
            }
            Type rawType = ((ParameterizedType) gi).getRawType();
            if (TypeUtils.isAssignable(rawType, interfaceType)) {
                ParameterizedType ptype = (ParameterizedType) gi;
                Object arg = ptype.getActualTypeArguments()[argumentIndex];
                if (arg instanceof Class) {
                    return (Class<?>) arg;
                }
                if (arg instanceof ParameterizedType) {
                    return (Class<?>) ((ParameterizedType) arg).getActualTypeArguments()[0];
                }
            }
        }
        return null;
    }

    public static boolean isCollection(Class<?> type) {
        return Collection.class.isAssignableFrom(type) || type.isArray();
    }

    public static boolean isGenericCollection(Class<?> type) {
        if (type == null) return false;
        return isCollection(type) && type.getTypeParameters().length > 0;
    }

    public static Class<?> getGenericType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) type;
            return (Class<?>) ptype.getActualTypeArguments()[0];
        } else {
            throw new TechnicalException("getGenericType failed");
        }
    }

    /*
    public static ParameterizedType constructParametricType(Class<?> parametrized, Class<?> parameterClass) {
        JavaType type = new ObjectMapper().getTypeFactory().constructParametricType(parametrized, parameterClass);
        return (ParameterizedType)type;
    }*/

    @SneakyThrows
    public static <E> E newInstance(Class<E> clazz) {
        return clazz.getConstructor().newInstance();
    }

    // private static final Map<Class<?>, Class<?>> TYPES_CACHE = new ConcurrentHashMap<>();


}
