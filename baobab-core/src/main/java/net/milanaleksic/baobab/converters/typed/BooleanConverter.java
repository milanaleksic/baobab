package net.milanaleksic.baobab.converters.typed;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 4:32 PM
 */
public class BooleanConverter extends TypedConverter<Boolean> {

    @Override
    public Boolean getValueFromJson(JsonNode node) {
        return node.asBoolean();
    }

}
