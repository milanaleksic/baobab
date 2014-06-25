package net.milanaleksic.baobab.util.lambda;

@FunctionalInterface
public interface SupplierWithCheckedException<T> {
    T get() throws Exception;
}
