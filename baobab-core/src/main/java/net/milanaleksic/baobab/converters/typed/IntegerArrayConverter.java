package net.milanaleksic.baobab.converters.typed;

import net.milanaleksic.baobab.TransformerException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import javax.inject.Inject;
import java.io.IOException;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 3:23 PM
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class IntegerArrayConverter extends TypedConverter<int[]> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    private IntegerConverter integerConverter;

    @Override
    public int[] getValueFromJson(JsonNode node) {
        try {
            final JsonNode[] jsonNodes = mapper.readValue(node, JsonNode[].class);
            int[] ofTheJedi = new int[jsonNodes.length];
            for (int i=0; i< jsonNodes.length; i++) {
                ofTheJedi[i] = integerConverter.getValueFromString(jsonNodes[i].asText());
            }
            return ofTheJedi;
        } catch (IOException e) {
            throw new TransformerException("Could not parse int array", e);
        }
    }

}
