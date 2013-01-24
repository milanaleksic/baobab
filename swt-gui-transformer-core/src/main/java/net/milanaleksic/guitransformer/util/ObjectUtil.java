package net.milanaleksic.guitransformer.util;

import com.google.common.base.*;
import com.google.common.collect.Iterables;
import net.milanaleksic.guitransformer.TransformerException;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * User: Milan Aleksic
 * Date: 1/23/13
 * Time: 1:48 PM
 */
public class ObjectUtil {

    public static void setFieldValueOnObject(Field field, final Object targetObject, final Object valueOfField) throws TransformerException {
        allowOperationOnField(field, new OperationOnField() {
            @Override
            public void operate(Field field) throws ReflectiveOperationException {
                field.set(targetObject, valueOfField);
            }
        });
    }

    public interface OperationOnField {
        void operate(Field field) throws ReflectiveOperationException;
    }

    public static void allowOperationOnField(Field field, OperationOnField operation) throws TransformerException {
        boolean wasPublic = Modifier.isPublic(field.getModifiers());
        if (!wasPublic)
            field.setAccessible(true);
        try {
            operation.operate(field);
        } catch (Exception e) {
            throw new TransformerException("Error while operating on field named " + field.getName(), e);
        } finally {
            if (!wasPublic)
                field.setAccessible(false);
        }
    }

    public static Optional<Field> getFieldByName(Object object, String fieldName) {
        for (Field field : object.getClass().getFields()) {
            if (field.getName().equals(fieldName)) {
                return Optional.of(field);
            }
        }
        return Optional.absent();
    }

    public static Optional<Method> getSetterByName(Object object, String setterName) {
        for (Method method : object.getClass().getMethods()) {
            if (method.getName().equals(setterName) && method.getParameterTypes().length == 1) {
                return Optional.of(method);
            }
        }
        return Optional.absent();
    }

    public static String getSetterForField(String fieldName) {
        return "set" + fieldName.substring(0, 1).toUpperCase(Locale.getDefault()) + fieldName.substring(1); //NON-NLS
    }

    public static Iterable<Field> getFieldsWithAnnotation(Class<?> clazz, final Class<? extends Annotation> annotation) {
        return Iterables.filter(Arrays.asList(clazz.getDeclaredFields()), new Predicate<Field>() {
            @Override
            public boolean apply(Field field) {
                return field.getAnnotation(annotation) != null;
            }
        });
    }

}
