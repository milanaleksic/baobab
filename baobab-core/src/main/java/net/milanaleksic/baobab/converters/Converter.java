package net.milanaleksic.baobab.converters;

import org.codehaus.jackson.JsonNode;

import java.util.Map;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 2:10 PM
 */
public interface Converter {

    public Object getValueFromJson(TransformationWorkingContext context, JsonNode value);

    void cleanUp();

}
