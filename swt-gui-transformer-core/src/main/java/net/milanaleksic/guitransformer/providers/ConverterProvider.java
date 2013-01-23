package net.milanaleksic.guitransformer.providers;

import com.google.common.base.Optional;
import net.milanaleksic.guitransformer.Converter;

public interface ConverterProvider {

    Converter provideConverterForClass(final Class<?> argType);

    <T> Optional<Converter<T>> provideExactTypeConverterForClass(final Class<T> type);

}
