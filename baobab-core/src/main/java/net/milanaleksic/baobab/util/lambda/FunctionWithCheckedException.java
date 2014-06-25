package net.milanaleksic.baobab.util.lambda;

@FunctionalInterface
public interface FunctionWithCheckedException<T, R> {
    R apply(T t) throws Exception;
}
