package net.milanaleksic.guitransformer.providers.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import net.milanaleksic.guitransformer.builders.Builder;
import net.milanaleksic.guitransformer.integration.loader.Loader;
import net.milanaleksic.guitransformer.providers.BuilderProvider;
import net.milanaleksic.guitransformer.util.*;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;

public class ConfigurableBuilderProvider implements BuilderProvider {

    private final Loader loader;

    private final AtomicReference<ImmutableMap<String, Builder<?>>> mapping = new AtomicReference<>(null);

    private static final String GUI_TRANSFORMER_CONVERTERS_EXTENSION_PROPERTIES = "/META-INF/guitransformer.builders.properties"; //NON-NLS

    @Inject
    public ConfigurableBuilderProvider(Loader loader) {
        this.loader = loader;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Builder<T> provideBuilderForName(String name) {
        if (mapping.get() == null)
            bootUpLazilyMapping();
        return (Builder<T>) mapping.get().get(name);
    }

    private void bootUpLazilyMapping() {
        final ImmutableMap.Builder<String, Builder<?>> builder = ImmutableMap.<String, Builder<?>>builder();
        Configuration.loadStringToInstanceMappingToBuilder("builders", builder, Optional.of(loader));
        mapping.compareAndSet(null, builder
                .putAll(PropertiesMapper.<Builder<?>>getStringToInstanceMappingFromPropertiesFile(GUI_TRANSFORMER_CONVERTERS_EXTENSION_PROPERTIES, Optional.of(loader)))
                .build());
    }

}
