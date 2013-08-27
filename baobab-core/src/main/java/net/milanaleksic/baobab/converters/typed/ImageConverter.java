package net.milanaleksic.baobab.converters.typed;

import net.milanaleksic.baobab.providers.ImageProvider;
import org.codehaus.jackson.JsonNode;
import org.eclipse.swt.graphics.Image;

import javax.inject.Inject;

/**
 * User: Milan Aleksic
 * Date: 4/21/12
 * Time: 7:30 PM
 */
public class ImageConverter extends TypedConverter<Image> {

    @Inject
    private ImageProvider imageProvider;

    @Override
    public Image getValueFromJson(JsonNode node) {
        return imageProvider.provideImageForName(node.asText());
    }

}
