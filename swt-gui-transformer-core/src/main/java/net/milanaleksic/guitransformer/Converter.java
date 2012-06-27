package net.milanaleksic.guitransformer;

import org.codehaus.jackson.JsonNode;

import java.lang.reflect.*;
import java.util.Map;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 2:10 PM
 */
public interface Converter<T> {

    void invoke(Method method, Object targetObject, JsonNode value, Map<String, Object> mappedObjects, Class<T> argType) throws TransformerException;

    void setField(Field field, Object targetObject, JsonNode value, Map<String, Object> mappedObjects) throws TransformerException;

    void cleanUp() ;

}
