package net.milanaleksic.guitransformer;

import com.google.common.base.Optional;

import javax.inject.Inject;
import java.util.Map;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 2:19 PM
 */
public class ConverterFactory {

    private Map<Class<?>, Converter<?>> registeredConverters;

    public ConverterFactory() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (registeredConverters == null)
                    return;
                for (Converter converter : registeredConverters.values()) {
                    converter.cleanUp();
                }
            }
        });
    }

    @Inject
    public void setRegisteredConverters(Map<Class<?>, Converter<?>> registeredConverters) {
        this.registeredConverters = registeredConverters;
    }

    public Converter getConverter(final Class<?> argType) {
        Converter converter = registeredConverters.get(argType);
        if (converter == null)
            return registeredConverters.get(Object.class);
        return converter;
    }

    @SuppressWarnings({"unchecked"})
    public <T> Optional<Converter<T>> getExactTypeConverter(final Class<T> type) {
        Converter<T> converter = (Converter<T>) registeredConverters.get(type);
        if (converter == null)
            return Optional.absent();
        return Optional.of(converter);
    }

}
