package net.milanaleksic.baobab.util;

@FunctionalInterface
public interface ReflectiveCheckedConsumer<T> {
    void accept(T t) throws ReflectiveOperationException;
}
