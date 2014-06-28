package net.milanaleksic.baobab.builders;

import java.util.Optional;

/**
 * User: Milan Aleksic
 * Date: 6/22/12
 * Time: 4:37 PM
 */
public class BuilderContext<T> {

    private final T builtElement;
    private final Optional<String> name;

    public BuilderContext(T builtElement, String name) {
        this.builtElement = builtElement;
        this.name = Optional.of(name);
    }
    public BuilderContext(T builtElement) {
        this.builtElement = builtElement;
        this.name = Optional.empty();
    }

    public T getBuiltElement() {
        return builtElement;
    }

    public Optional<String> getName() {
        return name;
    }
}
