package net.milanaleksic.baobab.integration.loader.impl;

import com.google.inject.Inject;
import com.google.inject.Injector;
import net.milanaleksic.baobab.integration.loader.Loader;

public class GuiceLoader implements Loader {

    private final Injector injector;

    @Inject
    public GuiceLoader(Injector injector) {
        this.injector = injector;
    }

    @Override
    public void load(Object raw) {
        injector.injectMembers(raw);
    }
}
