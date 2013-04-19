package net.milanaleksic.guitransformer.providers.impl;

import com.google.common.collect.ImmutableMap;
import com.typesafe.config.*;
import net.milanaleksic.guitransformer.providers.ShortcutsProvider;
import net.milanaleksic.guitransformer.util.Configuration;

import java.util.Map;

import static net.milanaleksic.guitransformer.util.PropertiesMapper.getStringToClassMappingFromPropertiesFile;

public class ConfigurableShortcutsProvider implements ShortcutsProvider {

    private static final String GUI_TRANSFORMER_SHORTCUTS_EXTENSION_PROPERTIES = "/META-INF/guitransformer.shortcuts.properties"; //NON-NLS

    private final ImmutableMap<String, Class<?>> mapping;

    public ConfigurableShortcutsProvider() {
        // this Configurable* does not need lazy load since no injection is needed
        final ImmutableMap.Builder<String, Class<?>> builder = ImmutableMap.builder();
        Configuration.loadStringToClassMappingToBuilder("shortcuts", builder);
        mapping = builder
                .putAll(getStringToClassMappingFromPropertiesFile(GUI_TRANSFORMER_SHORTCUTS_EXTENSION_PROPERTIES))
                .build();
    }

    @Override
    public Class<?> provideClassForShortcut(String name) {
        return mapping.get(name);
    }

}
