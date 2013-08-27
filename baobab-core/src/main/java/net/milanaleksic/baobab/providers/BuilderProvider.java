package net.milanaleksic.baobab.providers;

import net.milanaleksic.baobab.builders.Builder;

public interface BuilderProvider {

    <T> Builder<T> provideBuilderForName(String name);

}
