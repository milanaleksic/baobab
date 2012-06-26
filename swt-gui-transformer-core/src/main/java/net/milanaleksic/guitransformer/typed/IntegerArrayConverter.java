package net.milanaleksic.guitransformer.typed;

import net.milanaleksic.guitransformer.TransformerException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Map;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 3:23 PM
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class IntegerArrayConverter extends TypedConverter<int[]> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public int[] getValueFromJson(JsonNode node, Map<String, Object> mappedObjects) throws TransformerException {
        try {
            final JsonNode[] jsonNodes = mapper.readValue(node, JsonNode[].class);
            int[] ofTheJedi = new int[jsonNodes.length];
            for (int i=0; i< jsonNodes.length; i++) {
                ofTheJedi[i] = new IntegerConverter().getValueFromString(jsonNodes[i].asText());
            }
            return ofTheJedi;
        } catch (IOException e) {
            throw new TransformerException("Could not parse int array", e);
        }
    }

}
