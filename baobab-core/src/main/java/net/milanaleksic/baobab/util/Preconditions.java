package net.milanaleksic.baobab.util;

import net.milanaleksic.baobab.builders.BuilderContext;

import javax.annotation.Nullable;

public class Preconditions {

    public static void checkArgument(boolean expression, String errorMessage) {
        if (!expression)
            throw new IllegalArgumentException(errorMessage);
    }

    public static void checkState(boolean expression, @Nullable Object errorMessage) {
        if (!expression) {
            throw new IllegalStateException(String.valueOf(errorMessage));
        }
    }

    public static void checkNotNull(Object expression, String msg) {
        if (expression == null) {
            throw new NullPointerException(msg);
        }
    }
}
