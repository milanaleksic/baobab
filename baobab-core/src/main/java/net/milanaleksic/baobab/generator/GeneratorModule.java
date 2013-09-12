package net.milanaleksic.baobab.generator;

import com.google.inject.*;
import net.milanaleksic.baobab.converters.*;

public class GeneratorModule extends AbstractModule {

    @Override
    protected void configure() {
        Binder binder = binder();
        binder.bind(NodeProcessor.class).to(GeneratorNodeProcessor.class).in(Scopes.SINGLETON);
    }

}
