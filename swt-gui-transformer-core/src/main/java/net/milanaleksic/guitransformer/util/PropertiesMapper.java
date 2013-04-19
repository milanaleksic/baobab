package net.milanaleksic.guitransformer.util;

import com.google.common.base.*;
import com.google.common.collect.*;
import com.typesafe.config.*;
import net.milanaleksic.guitransformer.TransformerException;
import net.milanaleksic.guitransformer.integration.loader.Loader;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.*;

public class PropertiesMapper {

    public static Map<String, ? extends Class<?>> getStringToClassMappingFromPropertiesFile(String propertiesLocation) {
        Map<String, Class<?>> ofTheJedi = new HashMap<>();
        try (InputStream additionalShortcutsStream = PropertiesMapper.class.getResourceAsStream(propertiesLocation)) {
            if (additionalShortcutsStream == null)
                return ofTheJedi;
            final Properties properties = new Properties();
            properties.load(additionalShortcutsStream);
            for (Map.Entry entry : properties.entrySet()) {
                ofTheJedi.put(entry.getKey().toString(), Class.forName(entry.getValue().toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ofTheJedi;
    }

    public static <T> Map<String, T> getStringToInstanceMappingFromPropertiesFile(String propertiesLocation) {
        return getStringToInstanceMappingFromPropertiesFile(propertiesLocation, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> getStringToInstanceMappingFromPropertiesFile(String propertiesLocation, Optional<Loader> loader) {
        Map<String, T> ofTheJedi = new HashMap<>();
        try (InputStream additionalShortcutsStream = PropertiesMapper.class.getResourceAsStream(propertiesLocation)) {
            if (additionalShortcutsStream == null)
                return ofTheJedi;
            final Properties properties = new Properties();
            properties.load(additionalShortcutsStream);
            for (Map.Entry entry : properties.entrySet()) {
                T raw = (T) ObjectUtil.createInstanceForType(Class.forName(entry.getValue().toString()));
                if (loader.isPresent())
                    loader.get().load(raw);
                ofTheJedi.put(entry.getKey().toString(), raw);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ofTheJedi;
    }

    public static <T> Map<Class<?>, T> getClassToInstanceMappingFromPropertiesFile(String propertiesLocation) {
        return getClassToInstanceMappingFromPropertiesFile(propertiesLocation, Optional.<Loader>absent());
    }

    @SuppressWarnings("unchecked")
    public static <T> Map<Class<?>, T> getClassToInstanceMappingFromPropertiesFile(String propertiesLocation, Optional<Loader> loader) {
        Map<Class<?>, T> ofTheJedi = new HashMap<>();
        try (InputStream additionalShortcutsStream = PropertiesMapper.class.getResourceAsStream(propertiesLocation)) {
            if (additionalShortcutsStream == null)
                return ofTheJedi;
            final Properties properties = new Properties();
            properties.load(additionalShortcutsStream);
            for (Map.Entry entry : properties.entrySet()) {
                final Class<?> classWhichIsMaybeWrapper = Class.forName(entry.getKey().toString());
                final Class<?> clazz = Class.forName(entry.getValue().toString());

                // To be both Guice- and Spring- able, class must be initialized via no-arg constructor
                // thus, it is not allowed to use constructor injection, only property injection
                if (clazz.getConstructor() == null)
                    throw new RuntimeException("Transformer supports only extension classes with default constructor");
                T raw = (T) ObjectUtil.createInstanceForType(clazz);
                if (loader.isPresent())
                    loader.get().load(raw);
                try {
                    Class primitiveClass = (Class) classWhichIsMaybeWrapper.getField("TYPE").get(null);
                    ofTheJedi.put(primitiveClass, raw);
                } catch (NoSuchFieldException ignored) {
                    ofTheJedi.put(classWhichIsMaybeWrapper, raw);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ofTheJedi;
    }
}
