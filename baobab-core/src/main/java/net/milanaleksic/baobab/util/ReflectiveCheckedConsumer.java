package net.milanaleksic.baobab.util;

@FunctionalInterface
public interface ReflectiveCheckedConsumer<T> extends CheckedConsumer<T, ReflectiveOperationException> {
    void accept(T t) throws ReflectiveOperationException;
}
