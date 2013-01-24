package net.milanaleksic.guitransformer.converters.typed;

import net.milanaleksic.guitransformer.*;
import net.milanaleksic.guitransformer.converters.Converter;
import org.codehaus.jackson.JsonNode;

import java.util.Map;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 2:59 PM
 */
public abstract class TypedConverter<T> implements Converter {

    @Override
    public Object getValueFromJson(Object targetObject, JsonNode value, Map<String, Object> mappedObjects) throws TransformerException {
        return getValueFromJson(value);
    }

    protected abstract T getValueFromJson(JsonNode node) throws TransformerException;

    @Override
    public void cleanUp() {}

}
