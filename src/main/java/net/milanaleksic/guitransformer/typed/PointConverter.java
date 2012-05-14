package net.milanaleksic.guitransformer.typed;

import org.codehaus.jackson.JsonNode;
import org.eclipse.swt.graphics.Point;

import java.util.Map;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 2:58 PM
 */
public class PointConverter extends TypedConverter<Point> {

    @Override
    public Point getValueFromJson(JsonNode value, Map<String, Object> mappedObjects) {
        String[] nodeValue = value.asText().split(","); //NON-NLS
        int x = Integer.parseInt(nodeValue[0]);
        int y = Integer.parseInt(nodeValue[1]);
        return new Point(x, y);
    }
}
