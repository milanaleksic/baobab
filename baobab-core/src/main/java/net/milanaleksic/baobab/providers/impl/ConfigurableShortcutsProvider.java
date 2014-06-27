package net.milanaleksic.baobab.providers.impl;

import net.milanaleksic.baobab.providers.ShortcutsProvider;
import net.milanaleksic.baobab.util.Configuration;

import java.util.Map;

public class ConfigurableShortcutsProvider implements ShortcutsProvider {

    private final Map<String, Class<?>> mapping;

    public ConfigurableShortcutsProvider() {
        // this Configurable* does not need lazy load since no injection is needed
        mapping = Configuration.loadStringToClassMapping("net.milanaleksic.baobab.shortcuts");
    }

    @Override
    public Class<?> provideClassForShortcut(String name) {
        return mapping.get(name);
    }

}
