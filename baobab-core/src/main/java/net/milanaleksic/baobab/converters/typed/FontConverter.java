package net.milanaleksic.baobab.converters.typed;

import net.milanaleksic.baobab.TransformerException;
import org.codehaus.jackson.JsonNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

/**
 * User: Milan Aleksic
 * Date: 4/20/12
 * Time: 10:18 AM
 */
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
        if (!node.has(FIELD_STYLE)) {
            return ofTheJedi;
        }

        String[] styles = node.get(FIELD_STYLE).asText().split("\\|");
        for (String style : styles) {
            switch (style) {
                case FIELD_STYLE_BOLD:
                    ofTheJedi |= SWT.BOLD;
                    break;
                case FIELD_STYLE_ITALIC:
                    ofTheJedi |= SWT.ITALIC;
                    break;
                default:
                    throw new TransformerException("Unrecognized field style - " + style);
            }
        }
        return ofTheJedi;
    }

}
