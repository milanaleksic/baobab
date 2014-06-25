package net.milanaleksic.baobab.util.lambda;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Safe {

    public static <T> Supplier<T> safe(SupplierWithCheckedException<T> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                throw new RuntimeException("Wrapped exception", e);
            }
        };
    }

    public static <T> Consumer<T> safe(ConsumerWithCheckedException<T> consumer) {
        return (item) -> {
            try {
                consumer.consume(item);
            } catch (Exception e) {
                throw new RuntimeException("Wrapped exception", e);
            }
        };
    }

    public static <T, R> Function<T, R> safeFunction(FunctionWithCheckedException<T, R> function) {
        return (item) -> {
            try {
                return function.apply(item);
            } catch (Exception e) {
                throw new RuntimeException("Wrapped exception", e);
            }
        };
    }

}
