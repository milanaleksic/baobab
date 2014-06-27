package net.milanaleksic.baobab.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.milanaleksic.baobab.TransformerException;
import net.milanaleksic.baobab.integration.loader.Loader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * User: Milan Aleksic
 * Date: 4/19/13
 * Time: 9:46 AM
 */
public class Configuration {

    private static final Config reference = ConfigFactory.load();

    public static Map<String, Class<?>> loadStringToClassMapping(String configName) {
        Map<String, Class<?>> builder = new HashMap<>();
        final Config configuration = reference.getConfig(configName);
        configuration.root().unwrapped().entrySet().forEach(entry -> {
            try {
                builder.put(entry.getKey(), Class.forName(entry.getValue().toString()));
            } catch (ClassNotFoundException e) {
                throw new TransformerException("Configuration could not be loaded for entry: " + entry.getKey(), e);
            }
        });
        return Collections.unmodifiableMap(builder);
    }

    @SuppressWarnings("unchecked")
    public static <T> Map<Class<?>, T> loadClassToInstanceMapping(String configName, Optional<Loader> maybeLoader) {
        final Config configuration = reference.getConfig(configName);
        if (configuration.isEmpty())
            return Collections.emptyMap();
        Map<Class<?>, T> builder = new HashMap<>();
        configuration.root().unwrapped().entrySet().forEach(entry -> {
            try {
                final Class<?> classWhichIsMaybeWrapper = Class.forName(entry.getKey());
                final Class<?> clazz = Class.forName(entry.getValue().toString());

                // To be both Guice- and Spring- able, class must be initialized via no-arg constructor
                // thus, it is not allowed to use constructor injection, only property injection
                if (clazz.getConstructor() == null)
                    throw new TransformerException("Transformer supports only extension classes with default constructor");
                T raw = (T) ObjectUtil.createInstanceForType(clazz);
                maybeLoader.ifPresent(loader -> loader.load(raw));
                try {
                    Class primitiveClass = (Class) classWhichIsMaybeWrapper.getField("TYPE").get(null);
                    builder.put(primitiveClass, raw);
                } catch (NoSuchFieldException ignored) {
                    builder.put(classWhichIsMaybeWrapper, raw);
                }
            } catch (Exception e) {
                throw new TransformerException("Configuration could not be loaded for entry: " + entry.getKey(), e);
            }
        });
        return Collections.unmodifiableMap(builder);
    }

    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> loadStringToInstanceMapping(String configName, Optional<Loader> maybeLoader) {
        final Config configuration = reference.getConfig(configName);
        if (configuration.isEmpty())
            return Collections.emptyMap();
        Map builder = new HashMap<>();
        configuration.root().unwrapped().entrySet().forEach(entry -> {
            try {
                T raw = (T) ObjectUtil.createInstanceForType(Class.forName(entry.getValue().toString()));
                maybeLoader.ifPresent(loader -> loader.load(raw));
                builder.put(entry.getKey(), raw);
            } catch (Exception e) {
                throw new TransformerException("Configuration could not be loaded for entry: " + entry.getKey(), e);
            }
        });
        return Collections.unmodifiableMap(builder);
    }
}
