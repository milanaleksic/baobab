package net.milanaleksic.guitransformer.providers;

import net.milanaleksic.guitransformer.Builder;

public interface BuilderProvider {

    <T> Builder<T> provideBuilderForName(String name);

}
