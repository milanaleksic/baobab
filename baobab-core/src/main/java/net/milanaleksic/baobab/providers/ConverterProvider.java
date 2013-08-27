package net.milanaleksic.baobab.providers;

import com.google.common.base.Optional;
import net.milanaleksic.baobab.converters.Converter;
import net.milanaleksic.baobab.converters.typed.TypedConverter;

public interface ConverterProvider {

    Converter provideConverterForClass(final Class<?> argType);

    <T> Optional<TypedConverter<T>> provideTypedConverterForClass(final Class<T> type);

}
