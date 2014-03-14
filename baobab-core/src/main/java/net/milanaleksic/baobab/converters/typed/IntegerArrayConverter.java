package net.milanaleksic.baobab.converters.typed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Preconditions;

import javax.inject.Inject;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 3:23 PM
 */
public class IntegerArrayConverter extends TypedConverter<int[]> {

    @Inject
    private IntegerConverter integerConverter;

    @Override
    public int[] getValueFromJson(JsonNode node) {
        Preconditions.checkArgument(node instanceof ArrayNode, "Array expected as node value, but %s found", node.getClass().getName());
        ArrayNode nodeAsArray = (ArrayNode) node;
        int[] ofTheJedi = new int[nodeAsArray.size()];
        for (int i = 0; i < nodeAsArray.size(); i++) {
            ofTheJedi[i] = integerConverter.getValueFromString(nodeAsArray.get(i).asText());
        }
        return ofTheJedi;
    }

}
