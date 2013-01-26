package net.milanaleksic.guitransformer.converters;

import net.milanaleksic.guitransformer.TransformerException;
import org.codehaus.jackson.JsonNode;

import java.util.Map;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 2:10 PM
 */
public interface Converter {

    public Object getValueFromJson(Object targetObject, JsonNode value, Map<String, Object> mappedObjects) throws TransformerException;

    void cleanUp() ;

}
