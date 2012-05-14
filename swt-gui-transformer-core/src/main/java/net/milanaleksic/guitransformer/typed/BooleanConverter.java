package net.milanaleksic.guitransformer.typed;

import org.codehaus.jackson.JsonNode;

import java.util.Map;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 4:32 PM
 */
public class BooleanConverter extends TypedConverter<Boolean> {

    @Override
    public Boolean getValueFromJson(JsonNode node, Map<String, Object> mappedObjects) {
        return node.asBoolean();
    }

}
