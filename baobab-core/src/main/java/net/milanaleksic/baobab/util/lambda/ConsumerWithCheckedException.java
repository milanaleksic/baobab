package net.milanaleksic.baobab.util.lambda;

@FunctionalInterface
public interface ConsumerWithCheckedException<T> {
    void consume(T item) throws Exception;
}
