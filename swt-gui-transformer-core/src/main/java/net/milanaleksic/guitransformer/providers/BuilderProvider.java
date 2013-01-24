package net.milanaleksic.guitransformer.providers;

import net.milanaleksic.guitransformer.builders.Builder;

public interface BuilderProvider {

    <T> Builder<T> provideBuilderForName(String name);

}
