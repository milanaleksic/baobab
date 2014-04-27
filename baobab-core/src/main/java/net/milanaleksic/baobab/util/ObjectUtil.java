package net.milanaleksic.baobab.util;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import net.milanaleksic.baobab.TransformerException;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * User: Milan Aleksic
 * Date: 1/23/13
 * Time: 1:48 PM
 */
public class ObjectUtil {

    public static void setFieldValueOnObject(Field field, final Object targetObject, final Object valueOfField) {
        allowOperationOnField(field, safeField -> safeField.set(targetObject, valueOfField));
    }

    public static String getInternalNameForClass(String className) {
        return className.replace('.', '/');
    }

    @SuppressWarnings("unchecked")
    public static Class<Object> defineClass(String className, byte[] bytes) {
        try {
            Method method = ClassLoader.class.getDeclaredMethod("defineClass",
                    new Class[]{String.class, byte[].class, int.class, int.class});
            method.setAccessible(true);
            return (Class) method.invoke(WidgetCreator.class.getClassLoader(), className, bytes, 0, bytes.length);
        } catch (Exception e) {
            throw new TransformerException("Failure while defining widget creator class", e);
        }
    }

    public static void allowOperationOnField(Field field, ReflectiveCheckedConsumer<Field> operation) {
        boolean wasPublic = Modifier.isPublic(field.getModifiers());
        if (!wasPublic)
            field.setAccessible(true);
        try {
            operation.accept(field);
        } catch (ReflectiveOperationException e) {
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
        return Optional.empty();
    }

    public static Optional<Method> getSetterByName(Object object, String setterName) {
        for (Method method : getAllAvailablePublicAndInheritedMethodsForClass(object.getClass())) {
            if (method.getName().equals(setterName) && method.getParameterTypes().length == 1) {
                return Optional.of(method);
            }
        }
        return Optional.empty();
    }

    private static ConcurrentMap<Class<?>, Method[]> availablePublicAndInheritedMethodsForClass = Maps.newConcurrentMap();

    public static Method[] getAllAvailablePublicAndInheritedMethodsForClass(Class<?> clazz) {
        if (availablePublicAndInheritedMethodsForClass.containsKey(clazz))
            return availablePublicAndInheritedMethodsForClass.get(clazz);
        Method[] newValue = clazz.getMethods();
        availablePublicAndInheritedMethodsForClass.putIfAbsent(clazz, newValue);
        return newValue;
    }

    private static ConcurrentMap<Class<?>, Method[]> availableDeclaredMethodsForClass = Maps.newConcurrentMap();
    public static Method[] getAllAvailableDeclaredMethodsForClass(Class<?> clazz) {
        if (availableDeclaredMethodsForClass.containsKey(clazz))
            return availableDeclaredMethodsForClass.get(clazz);
        Method[] newValue = clazz.getDeclaredMethods();
        availableDeclaredMethodsForClass.putIfAbsent(clazz, newValue);
        return newValue;
    }

    public static String getSetterForField(String fieldName) {
        return "set" + fieldName.substring(0, 1).toUpperCase(Locale.getDefault()) + fieldName.substring(1); //NON-NLS
    }

    public static Iterable<Field> getFieldsWithAnnotation(Class<?> clazz, final Class<? extends Annotation> annotation) {
        return Iterables.filter(Arrays.asList(clazz.getDeclaredFields()), field -> {
            if (field == null)
                throw new TransformerException("field is null");
            return field.getAnnotation(annotation) != null;
        });
    }

    public static <T> T createInstanceForType(Class<T> type) throws ReflectiveOperationException {
        return ConstructorAccess.get(type).newInstance();
    }

}
