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
    public final void invoke(Method method, Object targetObject, JsonNode value, Map<String, Object> mappedObjects, Class<T> argType) throws TransformerException {
        try {
            method.invoke(targetObject, getValueFromJson(value, mappedObjects));
        } catch (TransformerException e) {
            throw e;
        } catch (Exception e) {
            throw new TransformerException("Wrapped invoke failed: ", e);
        }
    }

    @Override
    public final void setField(Field field, Object targetObject, JsonNode value, Map<String, Object> mappedObjects, Class<T> argType) throws TransformerException {
        try {
            field.set(targetObject, getValueFromJson(value, mappedObjects));
        } catch (IllegalAccessException e) {
            throw new TransformerException("Wrapped setField failed: ", e);
        }
    }

    public abstract T getValueFromJson(JsonNode node, Map<String, Object> mappedObjects) throws TransformerException;

    @Override
    public void cleanUp() {}

}
