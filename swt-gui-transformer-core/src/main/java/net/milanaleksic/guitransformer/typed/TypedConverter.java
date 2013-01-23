package net.milanaleksic.guitransformer.typed;

import net.milanaleksic.guitransformer.*;
import org.codehaus.jackson.JsonNode;

import java.lang.reflect.*;
import java.util.Map;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 2:59 PM
 */
public abstract class TypedConverter<T> implements Converter<T> {

    @Override
    public Object getValueFromJson(Object targetObject, JsonNode value, Map<String, Object> mappedObjects) throws TransformerException {
        return getValueFromJson(value);
    }

    protected abstract T getValueFromJson(JsonNode node) throws TransformerException;

    @Override
    public void cleanUp() {}

}
