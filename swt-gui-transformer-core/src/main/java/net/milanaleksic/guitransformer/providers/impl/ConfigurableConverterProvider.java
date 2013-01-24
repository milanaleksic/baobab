package net.milanaleksic.guitransformer.providers.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import net.milanaleksic.guitransformer.*;
import net.milanaleksic.guitransformer.loader.Loader;
import net.milanaleksic.guitransformer.providers.ConverterProvider;
import net.milanaleksic.guitransformer.util.PropertiesMapper;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;

public class ConfigurableConverterProvider implements ConverterProvider {

    private final Loader loader;

    private final AtomicReference<ImmutableMap<Class<?>, Converter<?>>> mapping = new AtomicReference<>(null);

    private static final String GUI_TRANSFORMER_CONVERTERS_PROPERTIES = "/META-INF/guitransformer.converters-default.properties"; //NON-NLS
    private static final String GUI_TRANSFORMER_CONVERTERS_EXTENSION_PROPERTIES = "/META-INF/guitransformer.converters.properties"; //NON-NLS

    @Inject
    public ConfigurableConverterProvider(Loader loader) {
        this.loader = loader;
    }

    private void bootUpLazilyMapping() {
        final boolean success = mapping.compareAndSet(null, ImmutableMap.<Class<?>, Converter<?>>builder()
                .putAll(PropertiesMapper.<Converter<?>>getClassToInstanceMappingFromPropertiesFile(GUI_TRANSFORMER_CONVERTERS_PROPERTIES, Optional.of(loader)))
                .putAll(PropertiesMapper.<Converter<?>>getClassToInstanceMappingFromPropertiesFile(GUI_TRANSFORMER_CONVERTERS_EXTENSION_PROPERTIES, Optional.of(loader)))
                .build());
        if (success)
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    for (Converter converter : mapping.get().values()) {
                        converter.cleanUp();
                    }
                }
            });
    }

    @SuppressWarnings("unchecked")
    private <T> Converter<T> doProvideConverterForClass(Class<?> clazz) {
        if (mapping.get() == null)
            bootUpLazilyMapping();
        return (Converter<T>) mapping.get().get(clazz);
    }

    @Override
    public Converter<?> provideConverterForClass(final Class<?> argType) {
        Converter<?> converter = doProvideConverterForClass(argType);
        if (converter == null)
            return (Converter) doProvideConverterForClass(Object.class);
        return converter;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> Optional<Converter<T>> provideExactTypeConverterForClass(final Class<T> type) {
        Converter<T> converter = (Converter<T>) doProvideConverterForClass(type);
        if (converter == null)
            return Optional.absent();
        return Optional.of(converter);
    }

}