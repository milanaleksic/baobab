package net.milanaleksic.baobab.converters.typed;

import com.fasterxml.jackson.databind.JsonNode;
import net.milanaleksic.baobab.TransformerException;
import net.milanaleksic.baobab.util.lambda.ExtraCollectors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import java.util.Arrays;

public class FontConverter extends TypedConverter<Font> {

    private static final String FIELD_NAME = "name"; //NON-NLS
    private static final String FIELD_HEIGHT = "height"; //NON-NLS
    private static final String FIELD_STYLE = "style"; //NON-NLS
    private static final String FIELD_STYLE_BOLD = "bold"; //NON-NLS
    private static final String FIELD_STYLE_ITALIC = "italic"; //NON-NLS

    private FontData getSystemFontData() {
        return Display.getDefault().getSystemFont().getFontData()[0];
    }

    @Override
    public Font getValueFromJson(JsonNode node) {
        FontData systemFontData = getSystemFontData();
        int style = parseStyle(systemFontData, node);
        int height = parseHeight(systemFontData, node);
        String fontName = parseFontName(systemFontData, node);
        return new Font(Display.getDefault(), fontName, height, style);
    }

    private String parseFontName(FontData systemFontData, JsonNode node) {
        if (!node.has(FIELD_NAME))
            return systemFontData.getName();
        return node.get(FIELD_NAME).asText();
    }

    private int parseHeight(FontData systemFontData, JsonNode node) {
        if (!node.has(FIELD_HEIGHT))
            return systemFontData.getHeight();
        return node.get(FIELD_HEIGHT).asInt();
    }

    private int parseStyle(FontData systemFontData, JsonNode node) {
        int ofTheJedi = systemFontData.getStyle();
        if (!node.has(FIELD_STYLE))
            return ofTheJedi;
        return Arrays.asList(node.get(FIELD_STYLE).asText().split("\\|"))
                .stream().reduce(ofTheJedi, (identity, style) -> {
                    switch (style) {
                        case FIELD_STYLE_BOLD:
                            return identity | SWT.BOLD;
                        case FIELD_STYLE_ITALIC:
                            return identity | SWT.ITALIC;
                        default:
                            throw new TransformerException("Unrecognized field style - " + style);
                    }
                }, ExtraCollectors::intOrCollect);
    }

}
