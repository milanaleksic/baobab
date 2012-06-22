package net.milanaleksic.guitransformer.builders;

/**
 * User: Milan Aleksic
 * Date: 6/22/12
 * Time: 4:37 PM
 */
public class BuilderContext<T> {

    private final T builtElement;
    private final String name;

    public BuilderContext(T builtElement, String name) {
        this.builtElement = builtElement;
        this.name = name;
    }
    public BuilderContext(T builtElement) {
        this.builtElement = builtElement;
        this.name = null;
    }

    public T getBuiltElement() {
        return builtElement;
    }

    public String getName() {
        return name;
    }
}
