package net.milanaleksic.guitransformer.providers;

import com.google.common.base.Optional;
import net.milanaleksic.guitransformer.converters.Converter;
import net.milanaleksic.guitransformer.converters.typed.TypedConverter;

public interface ConverterProvider {

    Converter provideConverterForClass(final Class<?> argType);

    <T> Optional<TypedConverter<T>> provideTypedConverterForClass(final Class<T> type);

}
