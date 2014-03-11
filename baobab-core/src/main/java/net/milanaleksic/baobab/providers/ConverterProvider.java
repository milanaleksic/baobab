package net.milanaleksic.baobab.providers;

import net.milanaleksic.baobab.converters.Converter;
import net.milanaleksic.baobab.converters.typed.TypedConverter;

import java.util.Optional;

public interface ConverterProvider {

    Converter provideConverterForClass(final Class<?> argType);

    <T> Optional<TypedConverter<T>> provideTypedConverterForClass(final Class<T> type);

}
